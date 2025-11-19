package com.bbey.neez.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {
    private String userId;
    private String currentPassword;
    private String newPassword;
}
