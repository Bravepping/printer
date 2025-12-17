package com.ping.MyInterface;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FileCheck {

    String [] supportedFileTypes() default {"docx", "pdf", "txt"};

    long maxFileSize() default 1024 * 1024 * 30;

    String errorMsg() default "文件格式或大小不符合要求";

}
