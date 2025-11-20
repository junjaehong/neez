// 메모만 수정 요청 DTO
package com.bbey.neez.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BizCardMemoUpdateRequest {

    @Schema(description = "메모 내용", example = "2월 말에 다시 연락해야 함")
    private String memo;
}
