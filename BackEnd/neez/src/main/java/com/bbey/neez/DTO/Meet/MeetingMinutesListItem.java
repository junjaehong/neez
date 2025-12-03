package com.bbey.neez.DTO.Meet;

import com.bbey.neez.entity.Meet.MeetingMinutes;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingMinutesListItem {

    @Schema(description = "회의록 ID", example = "1")
    private Long minutesId;

    @Schema(description = "회의 ID", example = "10")
    private Long meetingId;

    @Schema(description = "회의 제목", example = "[2025.12.03.T13:07:03]")
    private String meetingTitle;

    @Schema(description = "연결된 명함 ID (없을 수 있음)", example = "81")
    private Long bizCardId;

    @Schema(description = "요약본")
    private String summaryText;

    @Schema(description = "파일명 (파일로 저장된 경우)")
    private String fileName;

    @Schema(description = "생성 일시")
    private LocalDateTime createdAt;

    public static MeetingMinutesListItem fromEntity(MeetingMinutes mm) {
        return MeetingMinutesListItem.builder()
                .minutesId(mm.getIdx())
                .meetingId(mm.getMeeting() != null ? mm.getMeeting().getIdx() : null)
                .meetingTitle(mm.getMeeting() != null ? mm.getMeeting().getTitle() : null)
                .bizCardId(mm.getBizCard() != null ? mm.getBizCard().getIdx() : null)
                .summaryText(mm.getSummaryText())
                .fileName(mm.getFileName())
                .createdAt(mm.getCreatedAt())
                .build();
    }
}
