package com.ping.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ping.pojo.Files;
import com.ping.pojo.PrintJob;
import com.ping.utils.SystemUtil;
import com.spire.doc.Document;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.annotation.PreDestroy; // Spring Boot 3 + JDK 17/21 必须用这个
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.PageRanges;
import javax.print.attribute.standard.Sides;
import java.awt.print.PrinterJob;
import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Component
public class PrintJobConsumer {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private PrintJobService printJobService;

    @Autowired
    private FilesService filesService;

    @Autowired
    private SysConfigService sysConfigService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SystemUtil systemUtil;

    // Redis Key 定义
    private static final String PRINT_QUEUE_KEY = "print:queue";
    private static final String PRINT_QUEUE_JOB = "print:queue:job";

    // 超时设置：60秒
    private static final long PRINT_TIMEOUT_SECONDS = 60;

    /**
     * 单线程线程池：
     * 专门用于执行物理打印任务。
     * 使用单线程是为了保证打印机指令串行化，避免并发抢占导致打印机驱动报错或卡死。
     */
    private final ExecutorService printerExecutor = Executors.newSingleThreadExecutor();

    /**
     * 消费者主入口
     * 策略：Redis 优先 -> 数据库兜底
     * fixedDelay = 3000: 每次执行完等待3秒再开始下一轮
     */
    @Scheduled(fixedDelay = 3000)
    public void consumePrintTask() throws InterruptedException {
        PrintJob currentJob = null;
        String source = "";

//        int print_wait_time = Integer.parseInt(systemUtil.get("print_delay"));
        //延迟打印时间
//        Thread.sleep(print_wait_time * 1000L);

        try {
            // ================= 1. 优先尝试从 Redis 获取 =================
            // leftPop: 获取并移除列表左侧第一个元素
            String printJobIdStr = stringRedisTemplate.opsForList().leftPop(PRINT_QUEUE_KEY);

            if (StringUtils.hasText(printJobIdStr)) {
                // Redis List 命中，去 Hash 里拿具体数据
                Object jsonObj = stringRedisTemplate.opsForHash().get(PRINT_QUEUE_JOB, printJobIdStr);
                if (jsonObj != null) {
                    currentJob = objectMapper.readValue(jsonObj.toString(), PrintJob.class);
                    source = "Redis";
                    // 拿到任务后，清理 Hash 中的数据，保持 Redis 干净
                    stringRedisTemplate.opsForHash().delete(PRINT_QUEUE_JOB, printJobIdStr);
                }
            }

            // ================= 2. 如果 Redis 没数据，尝试从数据库获取 (兜底) =================
            if (currentJob == null) {
                // 查询条件：状态为 "待打印"，按创建时间正序（FIFO），只取 1 条
                QueryWrapper<PrintJob> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("status", "待打印")
                        .orderByAsc("start_time")
                        .last("LIMIT 1");

                currentJob = printJobService.getOne(queryWrapper);
                if (currentJob != null) {
                    source = "Database";
                }
            }

            // ================= 3. 如果都没有任务，直接返回 =================
            if (currentJob == null) {
                return;
            }

            log.info("获取到打印任务，来源: {}, 任务ID: {}", source, currentJob.getId());

            // ================= 4. 执行核心处理逻辑 =================
            processPrintJob(currentJob);

        } catch (Exception e) {
            log.error("获取或解析打印任务异常", e);
        }
    }

    /**
     * 核心处理逻辑：包含审批检查、超时控制
     */
    private void processPrintJob(PrintJob printJob) {
        try {
            // 1. 校验审批配置
            Map<String, String> allConfigs = sysConfigService.getAllConfigs();
            String isReviewStr = allConfigs.get("is_review");
            int isReview = (isReviewStr != null) ? Integer.parseInt(isReviewStr) : 0;
            int isLive = (printJob.getIsLive() != null) ? printJob.getIsLive() : 0;

            // 审批逻辑判断
            if (isReview == 1) {
                if (isLive == 0) {
                    log.info("任务需审批但未通过，更新状态为待审批，ID: {}", printJob.getId());
                    if (!"待审批".equals(printJob.getStatus())) {
                        printJob.setStatus("待审批");
                        printJobService.updateById(printJob);
                    }
                    return; // 结束处理，等待审批通过
                }
            }

            log.info("开始执行打印，ID: {}", printJob.getId());

            // 2. 更新状态为 "打印中"
            printJob.setStatus("打印中");
            printJobService.updateById(printJob);

            // 3. 【核心】异步提交打印任务，并控制超时
            Future<?> future = printerExecutor.submit(() -> {
                try {
                    doRealPrintLogic(printJob);
                } catch (Exception e) {
                    throw new RuntimeException(e); // 抛出异常供外层捕获
                }
            });

            try {
                // 阻塞等待结果，最多等待 60 秒
                future.get(Long.parseLong(systemUtil.get("print_timeout")), TimeUnit.SECONDS);

                // 4. 打印成功：更新状态
                printJob.setStatus("已完成");
                printJob.setEndTime(new Date());
                printJobService.updateById(printJob);
                log.info("打印任务 ID: {} 最终完成", printJob.getId());

            } catch (TimeoutException e) {
                // A. 超时处理
                log.error("打印任务超时 ({}秒), 强制取消. ID: {}", Long.parseLong(systemUtil.get("print_timeout")), printJob.getId());
                future.cancel(true); // 尝试中断线程

                printJob.setStatus("打印超时");
                printJobService.updateById(printJob);

            } catch (ExecutionException e) {
                // B. 打印内部报错
                log.error("打印执行出错: {}", e.getCause().getMessage());
                printJob.setStatus("打印失败");
                printJobService.updateById(printJob);

            } catch (InterruptedException e) {
                // C. 线程被中断
                Thread.currentThread().interrupt();
                log.error("打印线程被中断");
            }

        } catch (Exception e) {
            log.error("处理打印任务系统异常, ID: " + printJob.getId(), e);
            printJob.setStatus("系统异常");
            printJobService.updateById(printJob);
        }
    }

    /**
     * 核心打印逻辑
     * 包含功能：获取实际页数并反向更新 "0-0" 为 "1-N"
     */
    private void doRealPrintLogic(PrintJob printJob) throws Exception {
        Files fileInfo = filesService.getById(printJob.getFilesId());
        if (fileInfo == null) throw new RuntimeException("数据库无此文件记录");

        // 处理文件路径
        File sourceFile = new File(fileInfo.getFilePath());
        if (!sourceFile.exists()) throw new RuntimeException("本地文件不存在: " + sourceFile.getAbsolutePath());

        String fileName = sourceFile.getName().toLowerCase();
        PrinterJob printerJob = PrinterJob.getPrinterJob();

        // 查找并设置打印机
        PrintService targetPrinter = findPhysicalPrinter(printJob);
        if (targetPrinter != null) {
            printerJob.setPrintService(targetPrinter);
        } else {
            log.warn("未找到指定打印机，使用系统默认打印机");
        }

        // 设置打印份数
        if (printJob.getCount() != null && printJob.getCount() > 0) {
            printerJob.setCopies(printJob.getCount());
        }

        // === PDF 处理逻辑 ===
        if (fileName.endsWith(".pdf")) {
            try (PDDocument pdfDoc = Loader.loadPDF(sourceFile)) {
                // 1. 获取 PDF 真实总页数
                int totalPages = pdfDoc.getNumberOfPages();

                // 2. 如果是 0-0 (全部打印)，将其更新为实际范围 (如 1-5)
                // 这样外层 updateById 时，数据库里就会记录实际打印了哪些页
                if ("0-0".equals(printJob.getPage())) {
                    String realRange = "1-" + totalPages;
                    printJob.setPage(realRange);
                    log.info("PDF文件: {}, 解析总页数为 {}, 修正页码范围为: {}", fileName, totalPages, realRange);
                }

                printerJob.setPageable(new PDFPageable(pdfDoc));
                PrintRequestAttributeSet attr = new HashPrintRequestAttributeSet();

                // 双面打印
                if (Integer.valueOf(1).equals(printJob.getIsDouble())) {
                    attr.add(Sides.TWO_SIDED_LONG_EDGE);
                }
                // 份数
                if (printJob.getCount() != null) {
                    attr.add(new Copies(printJob.getCount()));
                }

                // 3. 应用页码范围 (此时 0-0 已经被改成 1-N 了，可以正常解析)
                applyPageRanges(attr, printJob.getPage());

                printerJob.print(attr);
            }
        }
        // === Word 处理逻辑 ===
        else if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
            Document wordDoc = new Document();
            try {
                wordDoc.loadFromFile(sourceFile.getAbsolutePath());

                // 1. 获取 Word 真实总页数
                int totalPages = wordDoc.getPageCount();

                // 2. 如果是 0-0，更新为实际范围
                if ("0-0".equals(printJob.getPage())) {
                    String realRange = "1-" + totalPages;
                    printJob.setPage(realRange);
                    log.info("Word文件: {}, 解析总页数为 {}, 修正页码范围为: {}", fileName, totalPages, realRange);
                }

                printerJob.setPrintable(wordDoc);
                PrintRequestAttributeSet attr = new HashPrintRequestAttributeSet();

                if (printJob.getCount() != null) attr.add(new Copies(printJob.getCount()));
                if (Integer.valueOf(1).equals(printJob.getIsDouble())) attr.add(Sides.TWO_SIDED_LONG_EDGE);

                // 3. 应用页码范围
                applyPageRanges(attr, printJob.getPage());

                printerJob.print(attr);
            } finally {
                wordDoc.close();
            }
        } else {
            throw new RuntimeException("不支持的文件格式: " + fileName);
        }
    }

    /**
     * 辅助方法：设置页码范围
     */
    private void applyPageRanges(PrintRequestAttributeSet attr, String pageStr) {
        // 如果上面逻辑把 "0-0" 改成了 "1-5"，这里就会进入 if 内部，把 PageRanges 加入属性集
        // 如果这里依然收到 "0-0" (比如文件解析页数失败)，则跳过，默认打印全部
        if (StringUtils.hasText(pageStr) && !"0-0".equals(pageStr)) {
            try {
                if (pageStr.contains("-")) {
                    String[] parts = pageStr.split("-");
                    int start = Integer.parseInt(parts[0]);
                    int end = Integer.parseInt(parts[1]);
                    // 这里可以加个防御，防止 start > end
                    if (start <= end) {
                        attr.add(new PageRanges(start, end));
                    }
                } else {
                    attr.add(new PageRanges(Integer.parseInt(pageStr)));
                }
            } catch (Exception e) {
                log.warn("页码解析失败: {}, 将打印全部页面", pageStr);
            }
        }
    }

    /**
     * 辅助方法：查找物理打印机
     */
    private PrintService findPhysicalPrinter(PrintJob printJob) {
        PrintService[] services = PrinterJob.lookupPrintServices();

        // 如果任务没指定打印机，用默认的
        if (printJob.getPrinterName() == null) {
            return PrintServiceLookup.lookupDefaultPrintService();
        }

        // 遍历查找匹配名称的打印机
        for (PrintService service : services) {
            if (printJob.getPrinterName().equalsIgnoreCase(service.getName())) {
                return service;
            }
        }

        // 找不到则返回默认
        return PrintServiceLookup.lookupDefaultPrintService();
    }

    /**
     * 容器销毁时关闭线程池，释放资源
     */
    @PreDestroy
    public void destroy() {
        if (printerExecutor != null) {
            log.info("正在关闭打印线程池...");
            printerExecutor.shutdown();
            try {
                if (!printerExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    printerExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                printerExecutor.shutdownNow();
            }
        }
    }
}