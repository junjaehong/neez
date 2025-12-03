// 명함 OCR 요청 DTO
package com.bbey.neez.DTO.cardRequest;

import javax.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BizCardOcrRequest {

        @Schema(description = "서버에 저장되어 있는 명함 이미지 파일명", example = "biz_1764235026802.png")
        @NotBlank(message = "이미지 파일명은 필수입니다.")
        private String fileName;

        @Schema(description = "명함에 함께 저장할 메모(선택). " +
                        "예: '첫 통화 시, 담당자 매우 바쁨'", example = "B2B 담당자, 11월 28일 첫 통화", nullable = true)
        private String memo;
}
