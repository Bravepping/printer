package com.ping.controller;


import com.ping.MyInterface.FileCheck;
import com.ping.model.vo.FileVo;
import com.ping.model.vo.FilesListVo;
import com.ping.pojo.Files;
import com.ping.pojo.User;
import com.ping.service.FilesService;
import com.ping.utils.ResultT;
import com.ping.utils.SystemUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping(value = "/file")
@Slf4j
public class FileController {

    private FilesService filesService;

    @Value("${file.upload-path}")
    private String uploadPath;
    public FileController(FilesService filesService) {
        this.filesService = filesService;
    }

    /**
     * 上传文件
     * @param file
     * @param session
     * @return
     */
    @PostMapping(value = "/upload")
    @FileCheck
    public ResultT<String> uploadFile(@RequestParam("file") MultipartFile file,
                                      HttpSession session) {

        Files files = new Files();
        // 1. 基础检查
        if (file.isEmpty()) {
            return ResultT.error("上传文件不能为空");
        }

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResultT.error("请先登录");
        }
        // 2. 构建安全的存储目录
        String username = user.getUsername();
        // 建议使用 File.separator 兼容 Windows/Linux，或者直接用 /
        File directory = new File(uploadPath + File.separator + username);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 3. 生成唯一文件名 (核心修改)
        // 获取原始文件名: "my-photo.jpg"
        String originalFilename = file.getOriginalFilename();
        // 提取后缀: ".jpg"
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        // 生成UUID: "550e8400-e29b-41d4-a716-446655440000.jpg"
        String newFileName = UUID.randomUUID().toString() + suffix;

        try {
            // 4. 保存文件
            File dest = new File(directory, newFileName);
            file.transferTo(dest);
            files.setCreateTime(new Date());
            files.setUserId(user.getId());
            files.setFilePath(directory.getAbsolutePath() + File.separator + newFileName);
            files.setFileName(originalFilename);
            files.setFileUuid(newFileName);
            files.setFileSize(file.getSize());
            filesService.save(files);
            // 返回给前端的通常是文件的访问 URL，而不仅仅是文件名
            return ResultT.success("上传成功，文件名：" + originalFilename);

        } catch (IOException e) {
            e.printStackTrace();
            return ResultT.error("上传错误: " + e.getMessage());
        }
    }
    /**
     * 下载文件
     * @param fileId
     * @param session
     * @param response
     */
    @GetMapping("/download")
    public void downloadFile(@RequestParam("fileId") Integer fileId,
                             HttpSession session,
                             HttpServletResponse response) {

        log.info("开始下载文件:{}",fileId);
        // 1. 获取当前登录用户
        User user = (User) session.getAttribute("user");
        if (user == null) {
            // 未登录处理：直接抛出异常或重定向
            throw new RuntimeException("请先登录");
        }

        Integer userId = user.getId();
        if (userId == null) {
            try {
                // 如果未登录，返回 JSON 格式的错误提示
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\": 401, \"msg\": \"未登录\"}");
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            // 2. 调用 Service 获取文件（包含权限验证逻辑）

            File file = filesService.getFileForDownload(fileId, userId);

            // 3. 设置响应头 (关键步骤)
            // 告诉浏览器这是一个需要下载的文件，而不是直接在页面显示
            response.reset();
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("utf-8");
            response.setContentLength((int) file.length());

            // 处理文件名中文乱码问题
            String fileName = filesService.getById(fileId).getFileName();
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename=" + encodedFileName);

            // 4. 读取文件并写入响应流
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                 OutputStream os = response.getOutputStream()) {

                byte[] buffer = new byte[1024 * 8]; // 8KB 缓冲区
                int i;
                while ((i = bis.read(buffer)) != -1) {
                    os.write(buffer, 0, i);
                }
                os.flush();
            }

        } catch (Exception e) {
            // 5. 异常处理
            // 如果文件还没开始写流就报错了，可以返回 JSON 错误
            // 如果流已经开启了，这里只能记录日志，因为很难再改写 Response 为 JSON 了
            e.printStackTrace();
            try {
                if (!response.isCommitted()) {
                    response.reset();
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\": 500, \"msg\": \"" + e.getMessage() + "\"}");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 文件列表
     * @param session
     * @param query
     * @param page
     * @param size
     * @return
     */
    @RequestMapping(value = "/list")
    public ResultT<FilesListVo> fileList(HttpSession session,
                                         @RequestParam(value = "query",defaultValue = "") String query,
                                         @RequestParam(value = "page",defaultValue = "1") int page,
                                         @RequestParam(value = "size",defaultValue = "10") int size){
        User user = (User) session.getAttribute("user");
        if (user == null){
            return ResultT.error("请先登录");
        }
        return ResultT.success(filesService.selectFileList(user.getId(),page,size,query));
    }
    /**
     * 删除文件，将status改为0
     * @param id
     * @param session
     * @return
     */
    @RequestMapping(value = "/delete")
    public ResultT<String> deleteFile(@RequestParam("id") Integer id, HttpSession session){
        User user =(User) session.getAttribute("user");
        if (user == null){
            return ResultT.error("请先登录");
        }
        Files files = filesService.getById(id);
        if (Objects.equals(files.getUserId(), user.getId())){
            files.setStatus(0);
            return filesService.updateById(files) ? ResultT.success("删除成功") : ResultT.error("删除失败");
        }else {
            return ResultT.error("您没有权限删除此文件");
        }
    }
}
