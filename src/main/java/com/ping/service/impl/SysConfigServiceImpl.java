package com.ping.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ping.pojo.SysConfig;
import com.ping.service.SysConfigService;
import com.ping.mapper.SysConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
* @author Administrator
* @description 针对表【sys_config(系统参数配置表)】的数据库操作Service实现
* @createDate 2025-12-15 10:18:16
*/
@Slf4j
@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig>
    implements SysConfigService{

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 定义缓存的前缀常量
    private static final String CACHE_KEY_GLOBAL = "sys:config:all"; // 场景1：全量缓存Key
    private static final String CACHE_KEY_PREFIX = "sys:config:";    // 场景2：单条缓存前缀
    /**
     * 批量更新配置
     * @param configs key=configKey, value=configValue
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateConfigs(Map<String, String> configs) {
        if (configs == null || configs.isEmpty()) {
            return;
        }
        // 1. 循环更新数据库 (利用 MyBatis-Plus 的 LambdaUpdate)
        // 注意：虽然是在循环中调用 update，但配置项通常只有十几项，性能影响可忽略。
        // 如果数据量巨大（上千条），则不建议这样写，应使用 SQL Case When 语法。
        configs.forEach((key, value) -> {
            // update sys_config set config_value = ? where config_key = ?
            boolean success = this.lambdaUpdate()
                    .eq(SysConfig::getConfigKey, key) // 根据 config_key 查询
                    .set(SysConfig::getConfigValue, value) // 更新值
                    // .set(SysConfig::getUpdateTime, new Date()) // 如果数据库没有配置自动更新时间，需手动加上
                    .update();
            if (!success) {
                // 可选：记录日志，说明某个 Key 更新失败（可能是 Key 不存在）
                System.err.println("配置项不存在或更新失败: " + key);
            }
        });
        // 2. 清除 Redis 缓存 (Cache Aside: 先更库，后删缓存)
        clearRedisCache(configs);
    }

    @Override
    public Map<String, String> getAllConfigs() {
        Map<Object, Object> cacheMap = null;

        // 1. 尝试查 Redis 缓存 (加 try-catch 防止 Redis 挂了导致报错停止)
        try {
            cacheMap = redisTemplate.opsForHash().entries(CACHE_KEY_GLOBAL);
        } catch (Exception e) {
            // 重点：捕获异常，只打印日志，不抛出，让代码继续往下走去查数据库
            log.error("Redis 连接失败或读取超时，准备降级查询数据库: {}", e.getMessage());
            // 这里的 cacheMap 是 null，自然会走到下面的数据库查询逻辑
        }

        // 如果缓存不为空，直接转换并返回
        if (cacheMap != null && !cacheMap.isEmpty()) {
            return cacheMap.entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> (String) e.getKey(),
                            e -> (String) e.getValue()
                    ));
        }

        log.info("Redis 缓存未命中或连接失败，正在查询数据库...");

        // 2. 缓存未命中（或者 Redis 挂了），查询数据库
        List<SysConfig> list = this.list();

        // 3. 将 List 转换为 Map<Key, Value>
        Map<String, String> configMap = list.stream()
                .collect(Collectors.toMap(
                        SysConfig::getConfigKey,
                        // 防止 value 为 null 导致转 map 报错，给个空字符串默认值
                        item -> item.getConfigValue() == null ? "" : item.getConfigValue()
                ));
        // 4. 回写 Redis 缓存 (重建缓存)
        // 这里的 if 判断很重要，只有数据库查到了数据才回写
        if (!configMap.isEmpty()) {
            try {
                // 也要加 try-catch！
                // 如果 Redis 确实挂了，上面的读操作报错被 catch 了，走到这里尝试回写依然会报错
                // 如果这里不 catch，项目还是会启动失败
                redisTemplate.opsForHash().putAll(CACHE_KEY_GLOBAL, configMap);
                redisTemplate.expire(CACHE_KEY_GLOBAL, 24, TimeUnit.HOURS);
                log.info("系统配置已回写至 Redis");
            } catch (Exception e) {
                log.error("Redis 服务异常，回写缓存失败（仅记录，不影响业务）: {}", e.getMessage());
            }
        }

        return configMap;
    }

    private void clearRedisCache(Map<String, String> configs) {
        redisTemplate.delete(CACHE_KEY_GLOBAL);
    }
}




