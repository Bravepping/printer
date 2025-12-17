package com.ping.model.vo;

import lombok.Data;

import java.util.Date;
@Data
public class PrintsVo {
    //打印任务主键id
    private Integer id;
    private Integer filesId;
    //文件名称
    private String fileName;
    //提交任务时间
    private Date startTime;
    //完成打印时间
    private Date endTime;
    //页码范围，0-0为全部页码
    private String page;
    //打印份数
    private Integer count;
    //是否双面打印
    private Integer isDouble;
    //打印状态
    private String status;
    //打印机名称
    private String printerName;
    //审批状态
    private Integer isLive;
}
