package com.bbey.neez.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponseDto<T> {

    private boolean success; // 성공 여부
    private String message;  // 메시지
    private T data;          // 실제 응답 데이터
}
