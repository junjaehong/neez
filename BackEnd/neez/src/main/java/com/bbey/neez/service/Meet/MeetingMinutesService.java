package com.bbey.neez.service.Meet;

import com.bbey.neez.component.MemoStorage;
import com.bbey.neez.DTO.Meet.MeetingMinutesDetailResponse;
import com.bbey.neez.DTO.Meet.MeetingMinutesListItem;
import com.bbey.neez.entity.BizCard.BizCard;
import com.bbey.neez.entity.Meet.Meeting;
import com.bbey.neez.entity.Meet.MeetingMinutes;
import com.bbey.neez.repository.BizCard.BizCardRepository;
import com.bbey.neez.repository.Meet.MeetingMinutesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MeetingMinutesService {

    private final MeetingService meetingService;
    private final MeetingMinutesRepository meetingMinutesRepository;
    private final BizCardRepository bizCardRepository;
    private final MemoStorage memoStorage;

    /**
     * 스트리밍 회의 종료 시,
     * - MeetingMinutes 테이블에 회의록을 별도로 저장
     * - (옵션) 특정 BizCard와 연결
     * - 기존 BizCard 메모에는 더 이상 회의록을 붙이지 않음
     */
    @Transactional
    public MeetingMinutes saveStreamingMinutes(Long userIdx,
                                               Long meetingId,
                                               Long bizCardId,
                                               String summaryText,
                                               String minutesText) {

        // 1. 회의 조회
        Meeting meeting = meetingService.getMeeting(meetingId);

        // 1-1. 소유자 검증 (Meeting.userIdx 는 primitive long 이라고 가정)
        if (userIdx == null || !userIdx.equals(meeting.getUserIdx())) {
            throw new IllegalArgumentException("해당 회의에 접근할 권한이 없습니다. meetingId=" + meetingId);
        }

        // 2. (옵션) 명함 조회
        BizCard bizCard = null;
        if (bizCardId != null) {
            bizCard = bizCardRepository.findById(bizCardId)
                    .orElseThrow(() ->
                            new IllegalArgumentException("명함을 찾을 수 없습니다. bizCardId=" + bizCardId));
        }

        // 3. 파일로도 저장 (선택) – Meet 폴더 하위로 저장
        String fileName = null;
        if (minutesText != null && !minutesText.isEmpty()) {
            try {
                StringBuilder block = new StringBuilder();
                block.append("[Meeting] ").append(meeting.getTitle()).append("\n\n")
                        .append("▶ Summary\n")
                        .append(summaryText != null ? summaryText : "")
                        .append("\n\n")
                        .append("▶ Full Transcript\n")
                        .append(minutesText)
                        .append("\n");

                // 회의록은 Meet/meeting-*.txt 로 저장
                fileName = memoStorage.save("Meet/meeting-", block.toString());
            } catch (IOException e) {
                // 파일 저장 실패해도 DB에는 남기고 싶으면 예외는 삼키고 로그만
                // log.warn("Failed to save meeting minutes file", e);
            }
        }

        // 4. DB에 MeetingMinutes 저장 (메모와 완전히 분리된 저장소)
        MeetingMinutes minutes = MeetingMinutes.builder()
                .meeting(meeting)
                .bizCard(bizCard)
                .summaryText(summaryText)
                .minutesText(minutesText)
                .fileName(fileName)
                .build();

        return meetingMinutesRepository.save(minutes);
    }

    // ===================== 조회용 서비스 (DTO 변환 포함) =====================

    /**
     * 현재 로그인한 사용자 기준 회의록 전체 리스트 (DTO)
     */
    @Transactional(readOnly = true)
    public List<MeetingMinutesListItem> getMinutesListDtoByUser(Long userIdx) {
        return meetingMinutesRepository.findByUserIdxOrderByCreatedAtDesc(userIdx)
                .stream()
                .map(MeetingMinutesListItem::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 현재 로그인한 사용자 기준 회의 ID로 회의록 상세 (DTO)
     */
    @Transactional(readOnly = true)
    public MeetingMinutesDetailResponse getMinutesDetailDtoByMeeting(Long userIdx, Long meetingId) {
        MeetingMinutes mm = meetingMinutesRepository
                .findByUserIdxAndMeetingId(userIdx, meetingId)
                .orElseThrow(() ->
                        new IllegalArgumentException("해당 회의에 대한 회의록을 찾을 수 없습니다. meetingId=" + meetingId));

        return MeetingMinutesDetailResponse.fromEntity(mm);
    }

    /**
     * 특정 명함과 연결된 회의록 리스트 (현재 로그인한 사용자 기준, DTO)
     */
    @Transactional(readOnly = true)
    public List<MeetingMinutesListItem> getMinutesListDtoByBizCard(Long userIdx, Long bizCardId) {
        // 1) 명함 존재 + 소유자 검증 (BizCard.userIdx 도 primitive long 이라고 가정)
        BizCard card = bizCardRepository.findById(bizCardId)
                .orElseThrow(() ->
                        new IllegalArgumentException("명함을 찾을 수 없습니다. bizCardId=" + bizCardId));

        if (userIdx == null || !userIdx.equals(card.getUserIdx())) {
            throw new IllegalArgumentException("해당 명함에 대한 접근 권한이 없습니다. bizCardId=" + bizCardId);
        }

        // 2) 해당 명함과 연결된 회의록 전체 조회 후, createdAt 기준 내림차순 정렬
        return meetingMinutesRepository.findAllByBizCard_Idx(bizCardId).stream()
                .sorted(Comparator.comparing(MeetingMinutes::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(MeetingMinutesListItem::fromEntity)
                .collect(Collectors.toList());
    }

    // ===================== 필요 시 엔티티 직접 반환용 =====================

    @Transactional(readOnly = true)
    public List<MeetingMinutes> getMinutesListByUser(Long userIdx) {
        return meetingMinutesRepository.findByUserIdxOrderByCreatedAtDesc(userIdx);
    }

    /**
     * meetingId 기준 엔티티 직접 반환
     */
    @Transactional(readOnly = true)
    public MeetingMinutes getMinutesDetailByMeeting(Long userIdx, Long meetingId) {
        return meetingMinutesRepository.findByUserIdxAndMeetingId(userIdx, meetingId)
                .orElseThrow(() ->
                        new IllegalArgumentException("해당 회의에 대한 회의록을 찾을 수 없습니다. meetingId=" + meetingId));
    }

    // ===================== 삭제 =====================

    /**
     * meetingId 기준 회의록 삭제
     * - 로그인 유저 소속 회의인지 확인
     * - MeetingMinutes 레코드 삭제
     * - fileName 이 있으면 실제 파일도 삭제 시도
     */
    @Transactional
    public void deleteMinutesByMeeting(Long userIdx, Long meetingId) {
        MeetingMinutes mm = meetingMinutesRepository
                .findByUserIdxAndMeetingId(userIdx, meetingId)
                .orElseThrow(() ->
                        new IllegalArgumentException("삭제할 회의록을 찾을 수 없습니다. meetingId=" + meetingId));

        // 파일 삭제 (있다면)
        String fileName = mm.getFileName();
        if (fileName != null && !fileName.isEmpty()) {
            try {
                memoStorage.delete(fileName);
            } catch (IOException e) {
                // 파일 삭제 실패는 치명적이지 않으면 로그만 남기고 넘겨도 됨
                // log.warn("Failed to delete meeting minutes file: {}", fileName, e);
            }
        }

        // DB 레코드 삭제
        meetingMinutesRepository.delete(mm);
    }
}
