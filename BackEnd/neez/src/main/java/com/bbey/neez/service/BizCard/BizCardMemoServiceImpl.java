package com.bbey.neez.service.BizCard;

import com.bbey.neez.component.MemoStorage;
import com.bbey.neez.entity.BizCard;
import com.bbey.neez.repository.BizCardRepository;
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

    @Override
    public String getBizCardMemoContent(Long id) throws IOException {
        BizCard card = bizCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BizCard not found: " + id));

        if (card.getMemo() == null || card.getMemo().isEmpty()) {
            return "";
        }
        return memoStorage.read(card.getMemo());
    }

    @Override
    public BizCard updateBizCardMemo(Long id, String memo) {
        BizCard card = bizCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BizCard not found: " + id));

        String fileName = "card-" + card.getIdx() + ".txt";
        try {
            memoStorage.write(fileName, memo);
            card.setMemo(fileName);
        } catch (Exception e) {
            System.out.println("memo update failed: " + e.getMessage());
        }

        card.setUpdatedAt(LocalDateTime.now());
        return bizCardRepository.save(card);
    }
}
