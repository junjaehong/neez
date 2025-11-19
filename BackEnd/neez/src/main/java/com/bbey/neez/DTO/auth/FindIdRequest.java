package com.bbey.neez.DTO.auth;

import lombok.Data;

@Data
public class FindIdRequest {
    private String name;
    private String email;
}
