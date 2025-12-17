package com.ping.model.vo;



import lombok.Data;

import java.util.Date;
@Data
public class FileVo {
    private Integer id;
    private String fileName;
    private Long fileSize;
    private Date createTime;
}
