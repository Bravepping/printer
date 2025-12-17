package com.ping.mapper;

import com.ping.pojo.Files;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author Administrator
* @description 针对表【files】的数据库操作Mapper
* @createDate 2025-12-10 21:48:44
* @Entity com.ping.pojo.Files
*/
public interface FilesMapper extends BaseMapper<Files> {
    Files getFileById(Integer id);
}




