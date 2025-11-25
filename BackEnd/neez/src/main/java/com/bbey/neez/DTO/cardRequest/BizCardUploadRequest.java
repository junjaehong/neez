package com.bbey.neez.DTO.cardRequest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Schema(description = "명함 업로드 요청")
public class BizCardUploadRequest {

    @Schema(description = "명함 이미지 파일", type = "string", format = "binary")
    @NotBlank(message = "이미지는 필수입니다.")
    private MultipartFile file;

    @Schema(description = "사용자 IDX", example = "1")
    @NotNull(message = "user_idx는 필수입니다.")
    private Long user_idx;
}
