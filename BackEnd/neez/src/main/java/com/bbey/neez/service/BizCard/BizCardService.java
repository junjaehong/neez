package com.bbey.neez.service.BizCard;

import com.bbey.neez.DTO.BizCardDto;
import com.bbey.neez.entity.BizCard;
import com.bbey.neez.entity.BizCardSaveResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface BizCardService {

    // 🔹 현재 로그인 유저 기준 목록 (/me)
    Page<BizCardDto> getMyBizCards(Pageable pageable);

    // 🔹 OCR/수기 공통 저장 로직
    BizCardSaveResult saveFromOcrData(Map<String, String> data, Long userIdx);

    // 🔹 수기 입력 저장 (내 명함 수기 등록은 /me/manual 에서 SecurityUtil로 userIdx 주입)
    BizCardSaveResult saveManual(Map<String, String> data, Long userIdx);

    // 🔹 단건 상세 (소유자 검증 포함)
    Map<String, Object> getBizCardDetail(Long id);

    BizCardDto getBizCardDetailDto(Long id);

    // 🔹 수정 (소유자 검증 + 회사 재매칭 옵션)
    BizCard updateBizCard(Long idx, Map<String, String> data, boolean rematchCompany);

    // 🔹 삭제/복구 (소유자 검증)
    void deleteBizCard(Long id);

    void restoreBizCard(Long id);

    // 🔹 내 명함 검색 (/me/search)
    Page<BizCardDto> searchMyBizCards(String keyword, Pageable pageable);

    // 🔹 내 삭제된 명함 목록 (/me/deleted)
    Page<BizCardDto> getMyDeletedBizCards(Pageable pageable);

    // 🔹 내 명함 개수 (/me/count)
    long countMyBizCards();

    // 🔹 내 명함 중복 여부 (/me/exists)
    boolean existsMyBizCard(String name, String email);
}
