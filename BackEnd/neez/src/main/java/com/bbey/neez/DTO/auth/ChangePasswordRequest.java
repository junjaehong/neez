package com.bbey.neez.DTO.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {
    private String userId;
    private String currentPassword;
    private String newPassword;
}