package com.bbey.neez.dto.auth;

import lombok.Data;

@Data
public class FindIdRequest {
    private String name;
    private String email;
}
