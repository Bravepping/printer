package com.ping.utils;

import com.ping.service.SysConfigService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Slf4j
@Component
public class SystemUtil {

    @Autowired
    private SysConfigService sysConfigService;

    // 使用内存缓存配置，避免每次查库
    // ConcurrentHashMap 保证读写线程安全
    private Map<String, String> configCache = new ConcurrentHashMap<>();

    /**
     * 初始化方法
     * @PostConstruct 保证在依赖注入完成后自动执行
     */
    @PostConstruct
    public void init() {
        log.info("正在加载系统配置...");
        refresh();
    }

    /**
     * 刷新配置 (当你在后台修改配置后，调用此方法更新缓存)
     */
    public void refresh() {
        Map<String, String> latestConfigs = sysConfigService.getAllConfigs();
        if (latestConfigs != null) {
            configCache.clear();
            configCache.putAll(latestConfigs);
            log.info("系统配置加载完成，共 {} 条", configCache.size());
        }
    }

    /**
     * 获取字符串配置
     */
    public String get(String key) {
        return configCache.getOrDefault(key, "");
    }
}
