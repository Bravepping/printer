package com.ping.model.vo;

import lombok.Data;

import java.util.Date;

@Data
public class UserInfoVo {

    private Integer id;// 主键id
    private String username;//用户名
    private Date lastLoginTime;//最后登录时间
    private Date createTime;//创建时间
    private Integer role;//角色 1.普通用户 2.免审核用户 3.管理员
    private String email;//邮箱
    private Integer status;//状态 1.正常 0.禁用 -1.删除
    private Integer printCount;//打印总数
}
