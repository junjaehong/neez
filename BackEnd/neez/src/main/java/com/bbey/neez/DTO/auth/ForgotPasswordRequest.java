package com.bbey.neez.DTO.auth;

import lombok.Data;

@Data
public class ForgotPasswordRequest {
    private String userId;
    private String email;
}
