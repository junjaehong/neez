package com.bbey.neez.dto.auth;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String userId;
    private String email;
}
