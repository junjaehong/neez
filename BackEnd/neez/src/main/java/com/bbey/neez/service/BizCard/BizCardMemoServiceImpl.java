package com.bbey.neez.service.BizCard;

import com.bbey.neez.component.MemoStorage;
import com.bbey.neez.entity.BizCard;
import com.bbey.neez.exception.AccessDeniedBizException;
import com.bbey.neez.exception.ResourceNotFoundException;
import com.bbey.neez.repository.BizCardRepository;
import com.bbey.neez.security.SecurityUtil;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;

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
        if (currentUserIdx == null || card == null || card.getUserIdx() != currentUserIdx) {
            throw new AccessDeniedBizException("해당 명함에 대한 메모 접근 권한이 없습니다.");
        }
    }

    @Override
    public String getBizCardMemoContent(Long id) throws IOException {
        BizCard card = bizCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BizCard not found: " + id));

        // 🔒 소유자 검증
        verifyOwnership(card);

        if (card.getMemo() == null || card.getMemo().isEmpty()) {
            return "";
        }

        return memoStorage.read(card.getMemo());
    }

    @Override
    public BizCard updateBizCardMemo(Long id, String memo) {
        BizCard card = bizCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BizCard not found: " + id));

        // 🔒 소유자 검증
        verifyOwnership(card);

        String fileName = "card-" + card.getIdx() + ".txt";
        try {
            memoStorage.write(fileName, memo);
            card.setMemo(fileName);
        } catch (IOException e) {
            // 파일 저장 실패 시에도 원인만 로깅하고 비즈니스 예외로 감싸서 던짐
            System.out.println("memo update failed: " + e.getMessage());
            throw new RuntimeException("메모 저장 중 오류가 발생했습니다.");
        }

        card.setUpdatedAt(LocalDateTime.now());
        return bizCardRepository.save(card);
    }
}
