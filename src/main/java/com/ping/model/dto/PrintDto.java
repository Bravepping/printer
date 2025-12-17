package com.ping.model.dto;

import lombok.Data;

@Data
public class PrintDto {
    private Integer fileId;//文件id
    //如果页码为0-0，则打印所有页
    private Integer pageStart=0;//页码开始
    private Integer pageEnd=0;//页码结束
    private Integer count=1;//数量
    //是否双面打印
    private Integer isDoubleSided = 1;
    //打印机名称
    private String printerName;
}
