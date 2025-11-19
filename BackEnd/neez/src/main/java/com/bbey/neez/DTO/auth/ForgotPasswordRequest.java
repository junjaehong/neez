package com.bbey.neez.dto.auth;

import lombok.Data;

@Data
public class ForgotPasswordRequest {
    private String userId;
    private String email;
}
