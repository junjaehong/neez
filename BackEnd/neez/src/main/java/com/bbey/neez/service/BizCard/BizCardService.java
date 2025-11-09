package com.bbey.neez.service.BizCard;

import com.bbey.neez.DTO.BizCardDto;
import com.bbey.neez.entity.BizCard;
import com.bbey.neez.entity.BizCardSaveResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.List;

public interface BizCardService {

    BizCardSaveResult saveFromOcrData(Map<String, String> data, Long userIdx);

    BizCardSaveResult saveManual(Map<String, String> data, Long userIdx);

    // 이건 기존처럼 Map으로도 주고,
    Map<String, Object> getBizCardDetail(Long id);

    // 필요하면 DTO로도 바로 주는 메서드 하나 더
    BizCardDto getBizCardDetailDto(Long id);

    BizCard updateBizCard(Long idx, Map<String, String> data);

    void deleteBizCard(Long id);

    void restoreBizCard(Long id);

    Page<BizCardDto> getBizCardsByUserIdx(Long userIdx, Pageable pageable);

    Page<BizCardDto> getDeletedBizCardsByUserIdx(Long userIdx, Pageable pageable);

    Page<BizCardDto> searchBizCards(Long userIdx, String keyword, Pageable pageable);

    long countBizCardsByUser(Long userIdx);

    boolean existsBizCard(Long userIdx, String name, String email);
}
