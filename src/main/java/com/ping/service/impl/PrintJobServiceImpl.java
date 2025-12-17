package com.ping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ping.mapper.UserMapper;
import com.ping.model.dto.PrintDto;
import com.ping.model.vo.*;
import com.ping.pojo.Files;
import com.ping.pojo.PrintJob;
import com.ping.pojo.SysConfig;
import com.ping.service.FilesService;
import com.ping.service.PrintJobService;
import com.ping.mapper.PrintJobMapper;
import com.ping.service.SysConfigService;
import com.ping.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.print.PrintService;
import java.awt.print.PrinterJob;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Administrator
 * @description 针对表【print_job】的数据库操作Service实现
 */
@Service
@Slf4j
public class PrintJobServiceImpl extends ServiceImpl<PrintJobMapper, PrintJob>
        implements PrintJobService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SysConfigService sysConfigService;

    // 引入 Jackson 工具类，用于将对象转为 JSON 字符串
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private FilesService filesService;
    @Autowired
    private PrintJobMapper printJobMapper;


    // 定义 Redis 键名常量
    private static final String PRINT_QUEUE_KEY = "print:queue";
    private static final String PRINT_QUEUE_JOB = "print:queue:job";
    @Autowired
    private UserMapper userMapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createPrintJob(PrintDto printDto, Integer userId) {
        // 1. 校验文件归属
        Files files = filesService.getOne(new QueryWrapper<Files>()
                .eq("id", printDto.getFileId())
                .eq("user_id", userId));
        if (files == null) {
            return false;
        }

        PrintJob printJob = new PrintJob();

        // 获取用户角色 (建议判空)
        Integer role = userMapper.getRoleByUserId(userId);
        role = (role == null) ? 0 : role;

        // ================== 改造点 1：简化配置获取 ==================
        // 直接调用我们之前加强过的 getAllConfigs()，不用在这里写 try-catch 或手动查库
        // 它内部已经处理了 Redis 挂掉的情况
        Map<String, String> allConfigs = sysConfigService.getAllConfigs();

        // 获取审核开关，默认为 "0" (关闭审核)
        String isReviewStr = allConfigs.get("is_review");
        boolean needReview = "1".equals(isReviewStr);

        // ================== 改造点 2：梳理状态逻辑 ==================
        // 逻辑：开启审核 且 用户不是管理员(role<2) -> 待审批
        //       否则 -> 待打印
        if (needReview && role < 2) {
            printJob.setIsLive(0);
            printJob.setStatus("待审批");
        } else {
            printJob.setIsLive(1);
            printJob.setStatus("待打印");
        }

        // 3. 数据组装
        printJob.setUserId(userId);
        printJob.setFilesId(printDto.getFileId());
        printJob.setPage(printDto.getPageStart() + "-" + printDto.getPageEnd());
        printJob.setCount(printDto.getCount());
        printJob.setIsDouble(printDto.getIsDoubleSided());
        printJob.setStartTime(new Date());
        printJob.setPrinterName(printDto.getPrinterName());

        // 4. 保存到 MySQL
        boolean isSaved = this.save(printJob);
        if (!isSaved) {
            return false;
        }
        log.info("打印任务已保存至数据库，ID: {}", printJob.getId());

        // ================== 改造点 3：Redis 推送 (软失败) ==================
        // 只有当状态是 "待打印" 时，才需要推送到队列
        // "待审批" 的任务不需要进队列，等审批通过后再推
        if ("待打印".equals(printJob.getStatus())) {
            try {
                String jobJson = objectMapper.writeValueAsString(printJob);

                // 推送 Hash
                stringRedisTemplate.opsForHash().put(PRINT_QUEUE_JOB, printJob.getId().toString(), jobJson);
                // 推送 List
                stringRedisTemplate.opsForList().rightPush(PRINT_QUEUE_KEY, printJob.getId().toString());

                log.info("任务已推送至 Redis 队列");
            } catch (Exception e) {
                // !!! 核心逻辑修改 !!!
                // 这里捕获异常，但是【不抛出】，也就是【不回滚】数据库
                // 因为消费者那边有数据库兜底，即使 Redis 挂了，消费者也能直接从 MySQL 查到这个任务
                log.error("Redis 服务异常，任务推送失败 (任务已存入DB，等待兜底机制处理): {}", e.getMessage());
            }
        }

        return true;
    }

    @Override
    //获取所有打印机
    public List<String> getPrinters() {
        log.info("获取所有打印机...");
        ArrayList<String> printerNames = new ArrayList<>();
        PrintService[] printServices = PrinterJob.lookupPrintServices();
        for (PrintService printService : printServices){
            String name = printService.getName().toLowerCase();
            //排除虚拟打印机
            if (name.contains("pdf")||name.contains("xps")||name.contains("note")){
                continue;
            }
            printerNames.add(printService.getName().toLowerCase());
        }
        return printerNames;
    }

    @Override
    public PrintsListVo getPrintJobsByUserId(Integer userId,Integer page,Integer size) {
        Page<PrintsVo> pageParam = new Page<>(page, size);
        IPage<PrintsVo> results = printJobMapper.getPrintJobsByUserIdMapper(pageParam, userId);
        PrintsListVo printsListVo = new PrintsListVo();
        QueryWrapper<PrintJob> wrapper_count = new QueryWrapper<>();
        long count;
        count = this.count(wrapper_count.eq("user_id", userId));
        // 1. 构造查询条件
        QueryWrapper<PrintJob> wrapper = new QueryWrapper<>();
        wrapper.select("status", "count(*) as count") // 指定查询出来的列
                .eq("user_id", userId)
                .groupBy("status"); // 按状态分组

// 2. 执行查询 (返回 Map 列表)
        List<Map<String, Object>> list = this.listMaps(wrapper);

// 3. 解析结果 (假设你需要转换成特定变量)

        long wait = 0;
        long error = 0;
        long success = 0;
        long cancel = 0;
        long timeout  = 0;
        long reject = 0;
        long pending = 0;
        long wait_review = printJobMapper.getWaitReviewCount(userId);

        for (Map<String, Object> map : list) {
            String status = (String) map.get("status");
            // 注意：count(*) 返回的类型通常是 Long，但也可能是 Integer，最好转一下
            long num = Long.parseLong(map.get("count").toString());
            if ("待打印".equals(status)) {
                wait = num;
            } else if ("打印失败".equals(status)) {
                error = num;
            } else if ("已完成".equals(status)) {
                success = num;
            }else if ("已取消".equals(status)) {
                cancel = num;
            }
            else if ("打印超时".equals(status)) {
                timeout = num;
            } else if ("审批拒绝".equals(status)) {
                reject = num;
            } else if ("打印中".equals(status)) {
                pending = num;
            }
        }
        printsListVo.setCurrent(results.getCurrent());
        printsListVo.setTotal(results.getTotal());
        printsListVo.setPages(results.getPages());
        printsListVo.setSize(results.getSize());
        printsListVo.setPrintWaitReview(wait_review);
        printsListVo.setPrintPending(pending);
        printsListVo.setPrintRejectReview(reject);
        printsListVo.setPrintTimeout(timeout);
        printsListVo.setPrintCount(count);
        printsListVo.setPrintWait(wait);
        printsListVo.setPrintError(error);
        printsListVo.setPrintSuccess(success);
        printsListVo.setPrintCancel(cancel);
        printsListVo.setRecords(results.getRecords());

        return printsListVo;
    }

    @Override
    public boolean cancelPrintJob(Integer printJobId) {
        try {
            stringRedisTemplate.opsForList().remove(PRINT_QUEUE_KEY, 1, printJobId.toString());
            stringRedisTemplate.opsForHash().delete(PRINT_QUEUE_JOB, printJobId.toString());
            PrintJob job = this.getById(printJobId);
            job.setStatus("已取消");
            this.updateById(job);
            return true;
        }catch (Exception e)
            {
            log.error("取消打印任务失败：{}", e.getMessage());
            return false;
        }
    }

    @Override
    public AdminPrintsListVo getAllPrintJobs(int page, int size,int type) {
        // 1. 分页查询主数据
        Page<AdminPrintsVo> pageParam = new Page<>(page, size);
        IPage<AdminPrintsVo> results = printJobMapper.getAllPrintJobsIdMapper(pageParam,type);

        // 2. 初始化返回对象
        AdminPrintsListVo printsListVo = new AdminPrintsListVo();

        // 设置分页基本信息
        printsListVo.setCurrent(results.getCurrent());
        printsListVo.setTotal(results.getTotal());
        printsListVo.setPages(results.getPages());
        printsListVo.setSize(results.getSize());
        printsListVo.setRecords(results.getRecords());

        // 设置符合条件的总记录数 (跟 Total 一样)
        printsListVo.setPrintCount(results.getTotal());

        // 3. 获取状态统计 (调用 Mapper 新写的方法)
        List<Map<String, Object>> list = printJobMapper.getPrintJobStatusStatistics();

        long wait = 0;
        long error = 0;
        long success = 0;
        long cancel = 0;
        long timeout  = 0;
        long reject = 0;
        long pending = 0;

        // 解析统计结果
        if (list != null) {
            for (Map<String, Object> map : list) {
                String status = (String) map.get("status");
                // 数据库 count 返回类型处理
                long num = Long.parseLong(map.get("count").toString());

                if ("待打印".equals(status)) {
                    wait = num;
                } else if ("打印失败".equals(status)) {
                    error = num;
                } else if ("已完成".equals(status)) {
                    success = num;
                } else if ("已取消".equals(status)) {
                    cancel = num;
                } else if ("打印超时".equals(status)) {
                    timeout = num;
                } else if ("审批拒绝".equals(status)) {
                    reject = num;
                } else if ("打印中".equals(status)) {
                    pending = num;
                }
            }
        }

        // 4. 获取待审核数量
        Integer waitReview = printJobMapper.getAllWaitReviewCount();
        printsListVo.setPrintWaitReview(waitReview != null ? waitReview.longValue() : 0L);

        // 5. 填入统计数据
        printsListVo.setPrintWait(wait);
        printsListVo.setPrintError(error);
        printsListVo.setPrintSuccess(success);
        printsListVo.setPrintCancel(cancel);
        printsListVo.setPrintTimeout(timeout);
        printsListVo.setPrintRejectReview(reject);
        printsListVo.setPrintPending(pending);

        return printsListVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approvePrintJob(Integer printJobId, Integer isLive) {
        String jobIdStr = printJobId.toString();
        try {
            // 1. 从 Redis Hash 中获取打印任务 (注意：这里用 opsForHash，且 Key 是 PRINT_QUEUE_JOB)
            Object jsonObj = stringRedisTemplate.opsForHash().get(PRINT_QUEUE_JOB, jobIdStr);
            String jobJson = (jsonObj != null) ? jsonObj.toString() : null;

            PrintJob printJob;

            if (StringUtils.hasText(jobJson)) {
                // A. 缓存命中
                printJob = objectMapper.readValue(jobJson, PrintJob.class);
            } else {
                // B. 缓存未命中（兜底查库）
                printJob = printJobMapper.selectById(printJobId);
                if (printJob == null) {
                    return false; // 任务不存在
                }
            }
            // 2. 更新状态逻辑
            if (isLive == 1) {
                // 注意：保持与 createPrintJob 中的状态一致，建议统一使用 "待打印"
                printJob.setStatus("待打印");
                printJob.setIsLive(1);
                log.info("审批通过打印任务，ID: {}", printJob.getId());
                //更新redis和数据库状态
            } else if (isLive == -1) {
                printJob.setStatus("审批拒绝");
                printJob.setIsLive(-1);
                log.info("审批拒绝打印任务，ID: {}", printJob.getId());
            }
            // 3. 更新 Redis 数据 (写回 Hash)
            String newJson = objectMapper.writeValueAsString(printJob);
            stringRedisTemplate.opsForHash().put(PRINT_QUEUE_JOB, jobIdStr, newJson);

            // 4. 【可选优化】如果审批通过，是否需要重新触发打印队列？
            // 如果你的打印端消费者之前因为"待审批"而丢弃了任务，这里需要重新把 ID 推入 List
//            if (isLive == 1) {
//                // 检查队列中是否需要重新排队（简单粗暴的做法是直接推入队尾，打印端做去重或状态校验）
//                printJob.setStatus("待打印");
//                stringRedisTemplate.opsForList().rightPush(PRINT_QUEUE_KEY, jobIdStr);
//            }

            // 如果审批拒绝，为了节省空间，也可以选择直接从 Redis Hash 中删除（看业务需求）
            if (isLive == -1) {
                stringRedisTemplate.opsForHash().delete(PRINT_QUEUE_JOB, jobIdStr);
                // 尝试从等待队列中移除（如果不确定是否在队列里，这一步可以省略，打印端读到状态为拒绝会自动跳过）
                stringRedisTemplate.opsForList().remove(PRINT_QUEUE_KEY, 0, jobIdStr);
            }
            // 5. 更新数据库
            int rows = printJobMapper.updateById(printJob);
            return rows > 0;

        } catch (Exception e) {
            log.error("审批打印任务失败: id={}, error={}", printJobId, e.getMessage());
            throw new RuntimeException("审批更新失败", e);
        }
    }
}