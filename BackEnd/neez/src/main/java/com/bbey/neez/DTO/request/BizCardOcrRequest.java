// 명함 OCR 요청 DTO
package com.bbey.neez.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BizCardOcrRequest {

    @Schema(description = "서버에 저장되어 있는 명함 이미지 파일명", example = "biz1.jpg", required = true)
    private String fileName;

    @Schema(description = "이 명함을 소유할 사용자 IDX", example = "1")
    private Long userIdx;
}
