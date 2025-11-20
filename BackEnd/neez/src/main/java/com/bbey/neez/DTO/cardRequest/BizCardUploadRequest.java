package com.bbey.neez.DTO.cardRequest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Schema(description = "명함 업로드 요청")
public class BizCardUploadRequest {

    @Schema(description = "명함 이미지 파일", type = "string", format = "binary", required = true)
    private MultipartFile file;

    @Schema(description = "사용자 IDX", example = "1", required = false)
    private Long user_idx;
}
