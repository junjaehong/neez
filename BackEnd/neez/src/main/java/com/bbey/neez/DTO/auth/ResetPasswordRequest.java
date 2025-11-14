package com.bbey.neez.DTO.auth;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String userId;
    private String email;
}
