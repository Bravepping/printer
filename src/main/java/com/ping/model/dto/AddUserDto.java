package com.ping.model.dto;

import lombok.Data;

@Data
public class AddUserDto {
    private String username;
    private String password;
    private String email;
}
