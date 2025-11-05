package com.bbey.neez.service;

import java.util.Map;
import com.bbey.neez.entity.BizCard;
import com.bbey.neez.entity.BizCardSaveResult;

public interface BizCardReaderService {

    // OCR로 파일 읽어서 Map으로 뽑는 거 (기존 그대로)
    Map<String, String> readBizCard(String fileName);

    // OCR로 뽑은 Map을 DB에 저장하는 거
    BizCardSaveResult saveBizCardFromOcr(Map<String, String> data, Long userIdx);

    // 수기 등록도 같은 로직 타게
    BizCardSaveResult saveManualBizCard(Map<String, String> data, Long userIdx);

    // ✅ 명함 + 회사명까지 묶어서 주는 메서드
    Map<String, Object> getBizCardDetail(Long id);

    BizCard updateBizCard(Long idx, Map<String, String> data);
}
