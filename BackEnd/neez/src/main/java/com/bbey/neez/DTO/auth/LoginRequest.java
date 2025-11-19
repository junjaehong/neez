package com.bbey.neez.dto.auth;

import lombok.Data;

@Data
public class LoginRequest {
    private String userId;
    private String password;
}
