package com.ping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.repository.AbstractRepository;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ping.model.vo.FileVo;
import com.ping.model.vo.FilesListVo;
import com.ping.pojo.Files;
import com.ping.service.FilesService;
import com.ping.mapper.FilesMapper;
import com.ping.utils.ResultT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author Administrator
* @description 针对表【files】的数据库操作Service实现
* @createDate 2025-12-10 21:48:44
*/
@Service
@Slf4j
public class FilesServiceImpl extends ServiceImpl<FilesMapper, Files>
    implements FilesService{

    @Autowired
    private FilesMapper filesMapper;

    @Override
    public FilesListVo selectFileList(Integer id, int page, int size, String query) {
        // 1. 确保 fileName 不为 null 或 空字符串
        boolean condition = StringUtils.hasText(query);
        FilesListVo filesListVo = new FilesListVo();
        QueryWrapper<Files> queryWrapper = new QueryWrapper<>();
        QueryWrapper<Files> queryUserId = queryWrapper
                .eq("user_id", id)
                .eq("status", 1)
                .like(condition, "file_name", query);
        IPage<Files> pages = new Page<>(page, size);
        IPage<Files> filesIPage = this.page(pages,queryUserId);
        filesListVo.setCurrent(filesIPage.getCurrent());
        filesListVo.setPages(filesIPage.getPages());
        filesListVo.setSize(filesIPage.getSize());
        filesListVo.setTotal(filesIPage.getTotal());
        List<Files> filesList = filesIPage.getRecords();
        List<FileVo> fileVos = filesList.stream().map(file -> {
            FileVo fileVo = new FileVo();
            BeanUtils.copyProperties(file, fileVo);
            return fileVo;
        }).collect(Collectors.toList());
        filesListVo.setRecords(fileVos);
        return filesListVo;
    }

    @Override
    public File getFileForDownload(Integer fileId, Integer userId) {
        // 1. 查询数据库记录
        Files files = filesMapper.getFileById(fileId);
        log.info("files:{}", files);
        if (files == null) {
            throw new RuntimeException("文件记录不存在");
        }

        // 2. 验证文件是否属于当前用户
        // 注意：根据你的业务，管理员可能也能下载，这里只写了用户本人的判断
        if (!files.getUserId().equals(userId)) {
            // 如果有管理员角色判断，可以在这里加： && !isAdmin(userId)
            throw new RuntimeException("无权下载该文件");
        }

        // 3. 获取物理文件
        File physicalFile = new File(files.getFilePath());

        // 如果你的数据库存的是相对路径，需要结合 uploadDir 使用：
        // File physicalFile = new File(uploadDir, files.getFilePath());

        if (!physicalFile.exists()) {
            throw new RuntimeException("磁盘上文件已丢失，请联系管理员");
        }

        return physicalFile;
    }
}




