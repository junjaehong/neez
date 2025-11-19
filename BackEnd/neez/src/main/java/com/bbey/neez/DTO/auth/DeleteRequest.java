package com.bbey.neez.DTO.auth;

import lombok.Data;

@Data
public class DeleteRequest {
    private String userId;
    private String password;
}
