// 명함 OCR 요청 DTO
package com.bbey.neez.DTO.cardRequest;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BizCardOcrRequest {

    @Schema(description = "서버에 저장되어 있는 명함 이미지 파일명", example = "biz1.jpg")
    @NotBlank(message = "이미지는 필수입니다.")
    private String fileName;

    @Schema(description = "이 명함을 소유할 사용자 IDX", example = "1")
    @NotNull(message = "user_idx는 필수입니다.")
    private Long userIdx;
}
