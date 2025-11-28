package com.bbey.neez.service.BizCard;

import com.bbey.neez.component.MemoStorage;
import com.bbey.neez.entity.BizCard.BizCard;
import com.bbey.neez.exception.AccessDeniedBizException;
import com.bbey.neez.exception.ResourceNotFoundException;
import com.bbey.neez.repository.BizCard.BizCardRepository;
import com.bbey.neez.security.SecurityUtil;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class BizCardMemoServiceImpl implements BizCardMemoService {

    private final BizCardRepository bizCardRepository;
    private final MemoStorage memoStorage;

    public BizCardMemoServiceImpl(BizCardRepository bizCardRepository, MemoStorage memoStorage) {
        this.bizCardRepository = bizCardRepository;
        this.memoStorage = memoStorage;
    }

    /**
     * 현재 로그인한 사용자가 해당 명함의 소유자인지 검증
     */
    private void verifyOwnership(BizCard card) {
        Long currentUserIdx = SecurityUtil.getCurrentUserIdx();
        if (currentUserIdx == null || card == null || !currentUserIdx.equals(card.getUserIdx())) {
            throw new AccessDeniedBizException("해당 명함에 대한 메모 접근 권한이 없습니다.");
        }
    }

    @Override
    public String getBizCardMemoContent(Long id) throws IOException {
        BizCard card = bizCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BizCard not found: " + id));

        verifyOwnership(card);

        String memoRef = card.getMemo();
        if (memoRef == null || memoRef.isEmpty()) {
            return "";
        }

        // 정상 케이스: 파일명으로 저장된 경우
        if (isFileName(memoRef)) {
            return memoStorage.read(memoRef);
        }

        // 옛날 잘못된 값(내용이 통째로 들어간 경우)은 그대로 내용으로 간주
        return memoRef;
    }

    @Override
    public BizCard updateBizCardMemo(Long id, String memo) {
        BizCard card = bizCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BizCard not found: " + id));

        verifyOwnership(card);

        String fileName = "card-" + card.getIdx() + ".txt";
        try {
            memoStorage.write(fileName, memo);
            card.setMemo(fileName); // DB에는 파일명만 저장
        } catch (IOException e) {
            System.out.println("memo update failed: " + e.getMessage());
            throw new RuntimeException("메모 저장 중 오류가 발생했습니다.");
        }

        card.setUpdatedAt(LocalDateTime.now());
        return bizCardRepository.save(card);
    }

    /**
     * 회의 요약 내용을 명함 메모에 덧붙이기
     */
    @Transactional
    public void appendMeetingSummaryToBizCard(Long bizCardId,
                                              String meetingTitle,
                                              String summary) {

        BizCard card = bizCardRepository.findById(bizCardId)
                .orElseThrow(() -> new ResourceNotFoundException("BizCard not found: " + bizCardId));

        verifyOwnership(card);

        String memoRef = card.getMemo();
        String oldContent = "";

        // 1) 기존 메모 내용 복원
        if (memoRef != null && !memoRef.isEmpty()) {
            if (isFileName(memoRef)) {
                // 파일명인 경우: 파일에서 읽기
                try {
                    oldContent = memoStorage.read(memoRef);
                } catch (IOException | InvalidPathException e) {
                    System.out.println("memo read failed: " + e.getMessage());
                    oldContent = "";
                }
            } else {
                // 옛날 잘못된 데이터: memo 컬럼에 내용이 직접 들어가 있음
                oldContent = memoRef;
                memoRef = null; // 새 파일로 마이그레이션
            }
        }

        // 2) 새 블럭 만들기
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy.MM.dd.'T'HH:mm:ss"));

        StringBuilder blockBuilder = new StringBuilder();
        blockBuilder.append("[").append(timestamp).append("]\n");
        if (meetingTitle != null && !meetingTitle.isEmpty()) {
            blockBuilder.append(meetingTitle).append("\n");
        }
        blockBuilder.append(summary != null ? summary : "");

        String newBlock = blockBuilder.toString();

        String newContent;
        if (oldContent == null || oldContent.trim().isEmpty()) {
            newContent = newBlock;
        } else {
            newContent = oldContent
                    + "\n\n------------------------------\n\n"
                    + newBlock;
        }

        // 3) 파일에 쓰기 (기존 파일명 재사용, 없으면 새로 생성)
        String fileName = (memoRef != null && !memoRef.isEmpty())
                ? memoRef
                : ("card-" + card.getIdx() + ".txt");

        try {
            memoStorage.write(fileName, newContent);
            card.setMemo(fileName); // memo 필드는 항상 "파일명"만 유지
        } catch (IOException | InvalidPathException e) {
            System.out.println("memo append failed: " + e.getMessage());
            throw new RuntimeException("메모 저장 중 오류가 발생했습니다.");
        }

        card.setUpdatedAt(LocalDateTime.now());
        bizCardRepository.save(card);
    }

    /**
     * memo 문자열이 "정상적인 파일명처럼 보이는지" 여부
     * - 개행이 없어야 하고
     * - 보통 card-xxx.txt 같은 패턴
     */
    private boolean isFileName(String value) {
        if (value == null) return false;
        if (value.contains("\n") || value.contains("\r")) return false;
        // 최소한의 heuristic: .txt로 끝나면 파일명으로 취급
        return value.endsWith(".txt");
    }
}
