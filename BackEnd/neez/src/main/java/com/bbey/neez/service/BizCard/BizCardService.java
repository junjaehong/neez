package com.bbey.neez.service.BizCard;

import com.bbey.neez.DTO.BizCardDto;
import com.bbey.neez.entity.BizCard;
import com.bbey.neez.entity.BizCardSaveResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface BizCardService {

    // ✅ 현재 로그인한 사용자의 명함 목록 (isDeleted = false)
    Page<BizCardDto> getMyBizCards(Pageable pageable);

    // ✅ OCR 기반 저장 (명함 OCR용 – 필요 시 userIdx 직접 받을 수 있도록 유지)
    BizCardSaveResult saveFromOcrData(Map<String, String> data, Long userIdx);

    // ✅ 수기 등록 (컨트롤러에서 SecurityUtil로 userIdx 주입해서 사용)
    BizCardSaveResult saveManual(Map<String, String> data, Long userIdx);

    // ✅ 상세 조회 (단건)
    Map<String, Object> getBizCardDetail(Long id);

    BizCardDto getBizCardDetailDto(Long id);

    // ✅ 수정
    BizCard updateBizCard(Long idx, Map<String, String> data, boolean rematchCompany);

    // ✅ 삭제/복구
    void deleteBizCard(Long id);

    void restoreBizCard(Long id);

    // ✅ 삭제된 내 명함 목록
    Page<BizCardDto> getMyDeletedBizCards(Pageable pageable);

    // ✅ 내 명함 검색
    Page<BizCardDto> searchMyBizCards(String keyword, Pageable pageable);

    // ✅ 내 명함 개수
    long countMyBizCards();

    // ✅ 내 명함 중복 여부
    boolean existsMyBizCard(String name, String email);
}
