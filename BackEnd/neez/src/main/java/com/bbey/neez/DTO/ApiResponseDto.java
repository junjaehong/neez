package com.bbey.neez.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "공통 API 응답 래퍼")
public class ApiResponseDto<T> {

    @Schema(description = "성공 여부", example = "true")
    private boolean success;

    @Schema(description = "응답 메시지", example = "OK")
    private String message;

    @Schema(description = "실제 응답 데이터")
    private T data;

    public static <T> ApiResponseDto<T> ok(T data) {
        return new ApiResponseDto<>(true, null, data);
    }

    public static <T> ApiResponseDto<T> ok(String message, T data) {
        return new ApiResponseDto<>(true, message, data);
    }

    public static <T> ApiResponseDto<T> fail(String message) {
        return new ApiResponseDto<>(false, message, null);
    }
}
