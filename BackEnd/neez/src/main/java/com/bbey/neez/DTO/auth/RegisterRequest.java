package com.bbey.neez.DTO.auth;

import lombok.Data;

@Data
public class RegisterRequest {
    private String userId;
    private String password;
    private String name;
    private String email;
    private String phone;
}
