package com.ping.model.dto;

import lombok.Data;

@Data
public class EditUserDto {
    private Integer id;
    private String email;
    private Integer role;
    private Integer status;
}
