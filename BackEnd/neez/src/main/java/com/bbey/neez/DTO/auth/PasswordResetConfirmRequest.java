package com.bbey.neez.DTO.auth;

import lombok.Data;

@Data
public class PasswordResetConfirmRequest {
    private String email;
    private String code;
    private String newPassword;
}
