package com.bbey.neez.controller.Meet;

import com.bbey.neez.DTO.Meet.MeetingMinutesDetailResponse;
import com.bbey.neez.DTO.Meet.MeetingMinutesListItem;
import com.bbey.neez.security.SecurityUtil;
import com.bbey.neez.service.Meet.MeetingMinutesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/meetings/me/minutes")
@Tag(name = "Meeting Minutes API", description = "회의록 목록 조회 / 상세 조회 / 삭제 API")
@SecurityRequirement(name = "BearerAuth")
public class MeetingMinutesController {

        private final MeetingMinutesService meetingMinutesService;

        public MeetingMinutesController(MeetingMinutesService meetingMinutesService) {
                this.meetingMinutesService = meetingMinutesService;
        }

        // =========================================================
        // 1. 내 회의록 전체 리스트 조회
        // =========================================================
        @Operation(summary = "내 회의록 전체 리스트 조회", description = "로그인한 사용자가 가진 모든 회의록을 최신순으로 조회합니다.")
        @GetMapping
        public ResponseEntity<List<MeetingMinutesListItem>> getMyMinutesList() {
                Long userIdx = SecurityUtil.getCurrentUserIdx();

                List<MeetingMinutesListItem> response = meetingMinutesService.getMinutesListDtoByUser(userIdx);

                return ResponseEntity.ok(response);
        }

        // =========================================================
        // 2. 회의 ID 기준 회의록 단건 상세 조회
        // =========================================================
        @Operation(summary = "회의 ID 기준 회의록 상세 조회", description = "meetingId를 기준으로 해당 회의의 회의록 상세 내용을 조회합니다.\n" +
                        "URL의 {meetingId} 는 회의록 PK가 아니라 Meeting의 ID 입니다.")
        @GetMapping("/{meetingId}")
        public ResponseEntity<MeetingMinutesDetailResponse> getMinutesDetailByMeeting(
                        @Parameter(description = "회의 ID", example = "53") @PathVariable Long meetingId) {
                Long userIdx = SecurityUtil.getCurrentUserIdx();

                MeetingMinutesDetailResponse response = meetingMinutesService.getMinutesDetailDtoByMeeting(userIdx,
                                meetingId);

                return ResponseEntity.ok(response);
        }

        // =========================================================
        // 3. 특정 명함과 연결된 회의록 리스트 조회
        // =========================================================
        @Operation(summary = "특정 명함과 연결된 회의록 리스트 조회", description = "bizCardId로 해당 명함과 연결된 회의록들을 조회합니다.")
        @GetMapping("/bizcard/{bizCardId}")
        public ResponseEntity<List<MeetingMinutesListItem>> getMinutesByBizCard(
                        @Parameter(description = "명함 ID", example = "81") @PathVariable Long bizCardId) {
                Long userIdx = SecurityUtil.getCurrentUserIdx();

                List<MeetingMinutesListItem> response = meetingMinutesService.getMinutesListDtoByBizCard(userIdx,
                                bizCardId);

                return ResponseEntity.ok(response);
        }

        // =========================================================
        // 4. 회의 ID 기준 회의록 삭제
        // =========================================================
        @Operation(summary = "회의 ID 기준 회의록 삭제", description = "meetingId를 기준으로 해당 회의에 연결된 회의록을 삭제합니다.\n" +
                        "파일로 저장된 회의록이 있다면 파일도 함께 삭제를 시도합니다.")
        @DeleteMapping("/{meetingId}")
        public ResponseEntity<Map<String, Object>> deleteMinutesByMeeting(
                        @Parameter(description = "회의 ID", example = "53") @PathVariable Long meetingId) {
                Long userIdx = SecurityUtil.getCurrentUserIdx();

                meetingMinutesService.deleteMinutesByMeeting(userIdx, meetingId);

                Map<String, Object> body = new HashMap<>();
                body.put("success", true);
                body.put("meetingId", meetingId);
                body.put("deleted", true);

                return ResponseEntity.ok(body);
        }
}
