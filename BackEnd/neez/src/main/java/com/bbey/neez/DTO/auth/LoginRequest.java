package com.bbey.neez.DTO.auth;

import lombok.Data;

@Data
public class LoginRequest {
    private String userId;
    private String password;
}
