package com.ping.service;

import com.ping.model.vo.FileVo;
import com.ping.model.vo.FilesListVo;
import com.ping.pojo.Files;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ping.utils.ResultT;

import java.io.File;
import java.util.List;

/**
* @author Administrator
* @description 针对表【files】的数据库操作Service
* @createDate 2025-12-10 21:48:44
*/
public interface FilesService extends IService<Files> {

    FilesListVo selectFileList(Integer id, int page, int size,String query);

    File getFileForDownload(Integer fileId, Integer userId);
}
