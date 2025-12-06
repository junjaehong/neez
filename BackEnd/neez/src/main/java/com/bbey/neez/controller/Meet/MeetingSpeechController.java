package com.bbey.neez.controller.Meet;

import com.bbey.neez.security.SecurityUtil;
import com.bbey.neez.service.Meet.MeetingMinutesService;
import com.bbey.neez.service.Meet.MeetingSpeechStreamService;
import com.bbey.neez.service.Meet.MeetingSummaryService;
import com.bbey.neez.service.Meet.MeetingService;
import com.bbey.neez.service.Meet.MeetingParticipantService;
import com.bbey.neez.entity.Meet.Meeting;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/meetings/me")
@Tag(
        name = "Meeting Speech / STT API",
        description = "회의 음성 업로드 · STT · 번역 · 요약 · 회의록 API\n\n" +
                "📌 meetingId란?\n" +
                "- 하나의 '회의 세션'을 구분하기 위한 ID입니다.\n" +
                "- /meetings/me (POST) 로 회의를 생성하면 meetingId를 돌려줍니다.\n" +
                "- 같은 회의 동안에는 항상 같은 meetingId를 사용하세요.\n"
)
@SecurityRequirement(name = "BearerAuth")
public class MeetingSpeechController {

    private final MeetingService meetingService;
    private final MeetingParticipantService meetingParticipantService;
    private final MeetingSummaryService summaryService;
    private final MeetingMinutesService minutesService;
    private final MeetingSpeechStreamService streamService;

    public MeetingSpeechController(
            MeetingService meetingService,
            MeetingParticipantService meetingParticipantService,
            MeetingSummaryService summaryService,
            MeetingMinutesService minutesService,
            MeetingSpeechStreamService streamService
    ) {
        this.meetingService = meetingService;
        this.meetingParticipantService = meetingParticipantService;
        this.summaryService = summaryService;
        this.minutesService = minutesService;
        this.streamService = streamService;
    }

    // =========================================================
    // 1. 회의 시작 (meeting 생성 + 참가자 연결)
    // =========================================================
    @Operation(
            summary = "회의 시작 (meeting 생성)",
            description = "회의를 생성하고 meetingId를 반환합니다.\n\n" +
                    "- sourceLang: 회의 원본 언어 코드 (예: ko)\n" +
                    "- targetLang: 번역 타겟 언어 코드 (예: en, 선택)\n" +
                    "- participantBizCardIds: 회의 참석자 명함 ID 목록\n" +
                    "\n" +
                    "회의 제목(title)은 자동으로 `[ yyyy.MM.dd.THH:mm:ss ]` 형식으로 생성됩니다."
    )
    @PostMapping
    public ResponseEntity<Map<String, Object>> startMeeting(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "회의 생성 요청",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            schema = @Schema(implementation = StartMeetingRequest.class)
                    )
            )
            @RequestBody StartMeetingRequest request
    ) {
        Long userIdx = SecurityUtil.getCurrentUserIdx();

        if (request.getSourceLang() == null || request.getSourceLang().isEmpty()) {
            throw new IllegalArgumentException("sourceLang(원본 언어 코드는 필수입니다.");
        }

        // 회의 생성 (title 자동 생성)
        Meeting meeting = meetingService.startMeeting(
                userIdx,
                request.getSourceLang()
        );

        Long meetingId = meeting.getIdx();
        String title = meeting.getTitle();

        // 참가자 명함 연결
        if (request.getParticipantBizCardIds() != null && !request.getParticipantBizCardIds().isEmpty()) {
            meetingParticipantService.setParticipants(meetingId, request.getParticipantBizCardIds());
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("userIdx", userIdx);
        payload.put("meetingId", meetingId);
        payload.put("title", title);
        payload.put("sourceLang", request.getSourceLang());
        payload.put("targetLang", request.getTargetLang());
        payload.put("participantBizCardIds", request.getParticipantBizCardIds());

        return ResponseEntity.ok(payload);
    }

    // DTO: 회의 시작 요청
    public static class StartMeetingRequest {

        @Schema(description = "원본 언어 코드", example = "ko", required = true)
        private String sourceLang;

        @Schema(description = "번역 타겟 언어 코드", example = "en")
        private String targetLang;

        @Schema(description = "참석자 명함 ID 목록", example = "[1, 2, 3]")
        private List<Long> participantBizCardIds;

        public String getSourceLang() {
            return sourceLang;
        }

        public void setSourceLang(String sourceLang) {
            this.sourceLang = sourceLang;
        }

        public String getTargetLang() {
            return targetLang;
        }

        public void setTargetLang(String targetLang) {
            this.targetLang = targetLang;
        }

        public List<Long> getParticipantBizCardIds() {
            return participantBizCardIds;
        }

        public void setParticipantBizCardIds(List<Long> participantBizCardIds) {
            this.participantBizCardIds = participantBizCardIds;
        }
    }

    // =========================================================
    // 2. 단일 파일 업로드 → STT + 요약
    // =========================================================
    @Operation(
            summary = "단일 회의 음성 업로드 + STT + 요약",
            description = "하나의 전체 음성 파일을 업로드하여 STT와 요약을 수행합니다.\n" +
                    "meetingId는 앞에서 생성한 회의 ID입니다.\n"
    )
    @PostMapping(
            value = "/{meetingId}/audio",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Map<String, Object>> upload(
            @Parameter(description = "회의 세션 ID (예: 1)")
            @PathVariable Long meetingId,

            @Parameter(description = "업로드할 회의 음성 파일")
            @RequestPart("file") MultipartFile file,

            @Parameter(description = "원본 언어 코드 (예: ko)", example = "ko")
            @RequestParam(value = "sourceLang", required = false) String sourceLang
    ) throws Exception {

        Long userIdx = SecurityUtil.getCurrentUserIdx();

        try {
            MeetingSummaryService.MeetingSummary result =
                    summaryService.summarize(userIdx, meetingId, file, sourceLang);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("userIdx", userIdx);
            payload.put("meetingId", meetingId);
            payload.put("filename", file.getOriginalFilename());
            payload.put("bytes", file.getSize());
            payload.put("text", result.transcript());
            payload.put("summary", result.summary());
            payload.put("speakers", result.speakerTurns());

            return ResponseEntity.ok(payload);

        } catch (IllegalArgumentException ex) {
            return badRequest(meetingId, ex.getMessage());
        } catch (IllegalStateException ex) {
            return serviceUnavailable(meetingId, ex.getMessage());
        } catch (RuntimeException ex) {
            return internalError(meetingId, ex.getMessage());
        }
    }

    // =========================================================
    // 3. 청크 업로드 (실시간 STT + 번역)
    // =========================================================
    @Operation(
            summary = "회의 음성 청크(STT Streaming) 업로드",
            description = "음성을 여러 조각(chunk)으로 나누어 업로드하면서 실시간 STT/번역을 수행합니다.\n" +
                    "같은 회의 도중에는 항상 같은 meetingId를 사용합니다. (예: 1)\n"
    )
    @PostMapping(
            value = "/{meetingId}/chunks",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Map<String, Object>> uploadChunk(
            @Parameter(description = "회의 세션 ID", example = "1")
            @PathVariable Long meetingId,

            @Parameter(
                    description = "업로드할 회의 음성 청크 파일",
                    content = @Content(
                            mediaType = "multipart/form-data",
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
            @RequestPart("file") MultipartFile file,

            @Parameter(description = "청크 순번", example = "1")
            @RequestParam(value = "index", required = false) Long index,

            @Parameter(description = "타겟 번역 언어", example = "en")
            @RequestParam(value = "targetLang", required = false) String targetLang,

            @Parameter(description = "원본 음성 언어", example = "ko-KR")
            @RequestParam(value = "sourceLang", required = false) String sourceLang
    ) throws Exception {

        Long userIdx = SecurityUtil.getCurrentUserIdx();

        try {
            MeetingSpeechStreamService.Segment segment =
                    streamService.processChunk(userIdx, meetingId, index, file, targetLang, sourceLang);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("userIdx", userIdx);
            payload.put("meetingId", meetingId);
            payload.put("index", segment.getIndex());
            payload.put("text", segment.getText());
            payload.put("receivedAt", segment.getReceivedAt());
            payload.put("bytes", segment.getBytes());
            payload.put("sourceLanguage", segment.getSourceLanguage());
            payload.put("targetLanguage", segment.getTargetLanguage());
            payload.put("translation", segment.getTranslatedText());
            payload.put("translationToKorean", segment.getTranslatedToKorean());
            payload.put("translationToTarget", segment.getTranslatedToTarget());
            // 실시간 번역 누적본을 내려줘서 프론트에서 바로 표시 가능
            payload.put("transcript", streamService.getTranscriptText(userIdx, meetingId));
            payload.put("translatedTranscript", streamService.getTranslatedTranscript(userIdx, meetingId));
            payload.put("segments", streamService.getSegments(userIdx, meetingId));

            return ResponseEntity.ok(payload);

        } catch (IllegalArgumentException ex) {
            return badRequest(meetingId, ex.getMessage());
        } catch (IllegalStateException ex) {
            return serviceUnavailable(meetingId, ex.getMessage());
        } catch (RuntimeException ex) {
            return internalError(meetingId, ex.getMessage());
        }
    }

    // =========================================================
    // 4. 현재까지 transcript / segment 조회
    // =========================================================
    @Operation(
            summary = "현재까지의 transcript 조회",
            description = "누적된 transcript(원본 텍스트)와 segments 목록을 조회합니다.\n" +
                    "meetingId는 동일 회의의 ID여야 합니다.\n"
    )
    @GetMapping("/{meetingId}/transcript")
    public ResponseEntity<Map<String, Object>> getTranscript(
            @Parameter(description = "회의 세션 ID", example = "1")
            @PathVariable Long meetingId
    ) {
        Long userIdx = SecurityUtil.getCurrentUserIdx();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("userIdx", userIdx);
        payload.put("meetingId", meetingId);
        payload.put("transcript", streamService.getTranscriptText(userIdx, meetingId));
        // 현재까지 모인 번역 누적본 추가
        payload.put("translatedTranscript", streamService.getTranslatedTranscript(userIdx, meetingId));
        payload.put("segments", streamService.getSegments(userIdx, meetingId));

        return ResponseEntity.ok(payload);
    }

    // =========================================================
    // 5. 회의 종료 + 최종 회의록 생성 (회의록 별도 저장)
    // =========================================================
    @Operation(
            summary = "스트리밍 회의 최종 회의록 생성 (회의 종료)",
            description = "지금까지 업로드된 청크를 기준으로 전체 transcript / 요약 / segment 목록을 생성하고,\n" +
                    "회의록을 MeetingMinutes 테이블에 별도로 저장합니다.\n" +
                    "bizCardId를 지정하면 해당 명함과 회의록을 연결할 수 있습니다.\n"
    )
    @PostMapping("/{meetingId}/minutes")
    public ResponseEntity<Map<String, Object>> finalizeStreamingMeeting(
            @Parameter(description = "회의 세션 ID", example = "1")
            @PathVariable Long meetingId,

            @Parameter(description = "요약을 연결할 명함 ID (선택)", example = "1")
            @RequestParam(value = "bizCardId", required = false) Long bizCardId
    ) {
        Long userIdx = SecurityUtil.getCurrentUserIdx();

        try {
            // 1) 지금까지의 transcript / segments 가져오기
            String originalTranscript = streamService.getTranscriptText(userIdx, meetingId);
            String koreanTranscript = originalTranscript; // 필요하면 여기서 번역/변환

            if (originalTranscript == null || originalTranscript.isEmpty()) {
                throw new IllegalArgumentException("회의 내용이 없습니다. 먼저 음성 청크를 업로드하세요.");
            }

            // 2) MeetingSummaryService를 사용해서 요약 생성
            //    (MeetingSummaryService에 meetingId, userIdx 기반 요약 메서드가 존재한다고 가정)
            String summary = summaryService.summarize(meetingId, userIdx);

            // 3) MeetingMinutes 테이블에 회의록 저장 (메모와 분리)
            minutesService.saveStreamingMinutes(
                    userIdx,
                    meetingId,
                    bizCardId,
                    summary,
                    koreanTranscript
            );

            // 4) 프론트 응답용 segments
            java.util.List<MeetingSpeechStreamService.Segment> segments =
                    streamService.getSegments(userIdx, meetingId);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("userIdx", userIdx);
            payload.put("meetingId", meetingId);
            payload.put("originalTranscript", originalTranscript);
            payload.put("koreanTranscript", koreanTranscript);
            payload.put("summary", summary);
            payload.put("segments", segments);

            return ResponseEntity.ok(payload);

        } catch (IllegalArgumentException ex) {
            return badRequest(meetingId, ex.getMessage());
        } catch (IllegalStateException ex) {
            return serviceUnavailable(meetingId, ex.getMessage());
        } catch (RuntimeException ex) {
            return internalError(meetingId, ex.getMessage());
        }
    }

    // =========================================================
    // 공통 에러 응답
    // =========================================================
    private ResponseEntity<Map<String, Object>> badRequest(Long meetingId, String msg) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("meetingId", meetingId);
        error.put("error", "BAD_REQUEST");
        error.put("message", msg);
        return ResponseEntity.badRequest().body(error);
    }

    private ResponseEntity<Map<String, Object>> serviceUnavailable(Long meetingId, String msg) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("meetingId", meetingId);
        error.put("error", "SERVICE_UNAVAILABLE");
        error.put("message", msg);
        return ResponseEntity.status(503).body(error);
    }

    private ResponseEntity<Map<String, Object>> internalError(Long meetingId, String msg) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("meetingId", meetingId);
        error.put("error", "INTERNAL_ERROR");
        error.put("message", msg);
        return ResponseEntity.status(500).body(error);
    }
}
