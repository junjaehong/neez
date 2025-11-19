package com.bbey.neez.dto.auth;

import lombok.Data;

@Data
public class DeleteRequest {
    private String userId;
    private String password;
}
