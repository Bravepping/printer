package com.ping.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class UserInfoListVo {
    private Long pages;//总页数
    private Long total;//总记录数
    private Long current;//当前页
    private Long size;//每页记录数
    private Integer userCount;
    private Integer role1Count;
    private Integer role2Count;
    private List<UserInfoVo> records;
}
