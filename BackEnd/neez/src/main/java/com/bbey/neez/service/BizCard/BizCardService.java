package com.bbey.neez.service.BizCard;

import com.bbey.neez.DTO.BizCardDto;
import com.bbey.neez.entity.BizCard.BizCard;
import com.bbey.neez.entity.BizCard.BizCardSaveResult;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface BizCardService {

    // 🔹 /me 전용 목록
    Page<BizCardDto> getMyBizCards(Pageable pageable);

    // 🔹 OCR/수기 공통 저장 (현재 로그인 유저 기준)
    BizCardSaveResult saveFromOcrData(Map<String, String> data);

    // 🔹 수기 저장 (현재 로그인 유저 기준)
    BizCardSaveResult saveManual(Map<String, String> data);

    // 🔹 상세 조회
    Map<String, Object> getBizCardDetail(Long id);

    BizCardDto getBizCardDetailDto(Long id);

    // 🔹 수정/삭제/복구
    BizCard updateBizCard(Long idx, Map<String, String> data, boolean rematchCompany);

    void deleteBizCard(Long id);

    void restoreBizCard(Long id);

    // 🔹 userIdx 기반 (관리자/통계용)
    Page<BizCardDto> getBizCardsByUserIdx(Long userIdx, Pageable pageable);

    Page<BizCardDto> getDeletedBizCardsByUserIdx(Long userIdx, Pageable pageable);

    Page<BizCardDto> searchBizCards(Long userIdx, String keyword, Pageable pageable);

    long countBizCardsByUser(Long userIdx);

    boolean existsBizCard(Long userIdx, String name, String email);

    // 🔹 /me 전용 검색/삭제목록/카운트/중복확인
    Page<BizCardDto> searchMyBizCards(String keyword, Pageable pageable);

    Page<BizCardDto> getMyDeletedBizCards(Pageable pageable);

    long countMyBizCards();

    boolean existsMyBizCard(String name, String email);
}
