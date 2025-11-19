package com.bbey.neez.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "API 응답 공통 DTO")
public class ApiResponseDto<T> {

    @Schema(description = "성공 여부", example = "true")
    private boolean success;

    @Schema(description = "응답 메시지", example = "ok")
    private String message;

    @Schema(description = "응답 데이터 (BizCardDto, MemoDto 등)")
    private T data;  
}
