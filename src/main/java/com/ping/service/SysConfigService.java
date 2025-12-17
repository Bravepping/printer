package com.ping.service;

import com.ping.pojo.SysConfig;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
* @author Administrator
* @description 针对表【sys_config(系统参数配置表)】的数据库操作Service
* @createDate 2025-12-15 10:18:16
*/
public interface SysConfigService extends IService<SysConfig> {

    void batchUpdateConfigs(Map<String, String> configs);

    Map<String, String> getAllConfigs();
}
