package com.ping.config;


import com.ping.service.SysConfigService;
import com.ping.utils.ResultT;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import com.ping.MyInterface.FileCheck;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Map;

@Aspect
@Component
public class FileCheckAspect {

    @Autowired
    private SysConfigService sysConfigService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Around("@annotation(fileCheck)")
    public Object checkFile(ProceedingJoinPoint joinPoint,FileCheck fileCheck) throws Throwable {

        long maxFileSize;
        String [] supportedFileTypes;
        String errorMsg;


        Object[] args = joinPoint.getArgs();
        for (Object arg : args){
            if (arg instanceof MultipartFile){
                MultipartFile file = (MultipartFile) arg;
                //取配置文件
                Map<String, String> allConfigs = sysConfigService.getAllConfigs();
                maxFileSize = Long.parseLong(allConfigs.get("max_file_size"));
                supportedFileTypes = allConfigs.get("allow_file_type").split( ",");
//                errorMsg = allConfigs.get("error_msg");
                if (file.getSize()/1024L/1024>maxFileSize){
                    return ResultT.error("文件大小超出限制");
                }
                String originalFilename = file.getOriginalFilename();
                if (originalFilename ==null || !originalFilename.contains(".")){
                    return "文件格式不符合要求";
                }
                String suffix = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
                boolean isSupported = Arrays.asList(supportedFileTypes).contains(suffix);
                if (!isSupported){
                    return ResultT.error(fileCheck.errorMsg() + "，仅支持: " + Arrays.toString(fileCheck.supportedFileTypes()));
                }
            }
        }
        return joinPoint.proceed();
    }
}
