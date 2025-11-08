package com.bbey.neez.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "명함 메모 DTO")
public class MemoDto {

    @Schema(description = "명함 ID", example = "10")
    private Long cardId;

    @Schema(description = "메모 내용", example = "미팅 일정: 11월 15일 오후 3시, 네이버 본사 방문 예정")
    private String memoContent;

    @Schema(description = "메모 파일명", example = "card-10.txt")
    private String memoFile;
}
