package com.bbey.neez.service;

import com.bbey.neez.DTO.BizCardDto;
import com.bbey.neez.entity.BizCard;
import com.bbey.neez.entity.BizCardSaveResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface BizCardService {

    // OCR에서 나온 데이터 저장
    BizCardSaveResult saveFromOcrData(Map<String, String> data, Long userIdx);

    // 수기 등록 저장
    BizCardSaveResult saveManual(Map<String, String> data, Long userIdx);

    // 상세 조회
    Map<String, Object> getBizCardDetail(Long id);

    // 기본 정보 수정
    BizCard updateBizCard(Long idx, Map<String, String> data);

    // 소프트 삭제
    void deleteBizCard(Long id);

    // 복원
    void restoreBizCard(Long id);

    // 사용자 명함 목록
    Page<BizCardDto> getBizCardsByUserIdx(Long userIdx, Pageable pageable);

    // 삭제된 명함 목록
    Page<BizCardDto> getDeletedBizCardsByUserIdx(Long userIdx, Pageable pageable);

    // 검색
    Page<BizCardDto> searchBizCards(Long userIdx, String keyword, Pageable pageable);

    // 개수
    long countBizCardsByUser(Long userIdx);

    // 중복 확인
    boolean existsBizCard(Long userIdx, String name, String email);
}
