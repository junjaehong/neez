package com.bbey.neez.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공통 API 응답 래퍼")
public class ApiResponseDto<T> {

    @Schema(description = "요청 성공 여부", example = "true")
    private boolean success;

    @Schema(description = "응답 메시지", example = "ok")
    private String message;

    // 제네릭은 실제 데이터 스키마로 대체됨
    @Schema(description = "응답 데이터")
    private T data;

    public ApiResponseDto() {
    }

    public ApiResponseDto(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(T data) {
        this.data = data;
    }
}
