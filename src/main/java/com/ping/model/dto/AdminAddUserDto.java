package com.ping.model.dto;

import lombok.Data;

@Data
public class AdminAddUserDto {

    private String username;

    private String password;

    private String email;

    private Integer role=1;

    private Integer status=1;
}
