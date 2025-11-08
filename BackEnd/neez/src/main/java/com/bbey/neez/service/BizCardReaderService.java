package com.bbey.neez.service;

import com.bbey.neez.DTO.BizCardDto;
import com.bbey.neez.entity.BizCard;
import com.bbey.neez.entity.BizCardSaveResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.Map;

public interface BizCardReaderService {

    /**
     * OCR로 명함 이미지 파일을 읽어서 필드별로 뽑아낸다.
     */
    Map<String, String> readBizCard(String fileName);

    /**
     * 명함 이미지를 저장소에 저장하고, 저장된 파일명을 반환한다.
     */
    String storeBizCardImage(org.springframework.web.multipart.MultipartFile file) throws IOException;

    /**
     * OCR로 뽑은 데이터를 실제 BizCard 엔티티로 저장한다.
     * 동일 명함(이름+이메일)이 있으면 existing=true로 반환.
     */
    BizCardSaveResult saveBizCardFromOcr(Map<String, String> data, Long userIdx);

    /**
     * 수기 등록도 OCR 저장 로직을 그대로 태운다.
     */
    BizCardSaveResult saveManualBizCard(Map<String, String> data, Long userIdx);

    /**
     * 명함 하나를 상세 조회해서 회사명, 메모내용까지 내려준다.
     */
    Map<String, Object> getBizCardDetail(Long id);

    /**
     * 명함의 기본 정보만 수정한다.
     */
    BizCard updateBizCard(Long idx, Map<String, String> data);

    /**
     * 명함의 메모만 따로 수정한다.
     */
    BizCard updateBizCardMemo(Long id, String memo);

    /**
     * 명함의 메모 내용만 문자열로 가져온다.
     */
    String getBizCardMemoContent(Long id) throws IOException;

    /**
     * 특정 사용자(userIdx)의 명함을 페이징으로 조회한다.
     * 삭제된(isDeleted=true) 명함은 제외한다.
     */
    Page<BizCardDto> getBizCardsByUserIdx(Long userIdx, Pageable pageable);

    /**
     * 명함을 실제로 삭제하지 않고 소프트 삭제 처리한다.
     */
    void deleteBizCard(Long id);

    /**
     * 사용자 명함 중에서 키워드로 검색한다.
     */
    Page<BizCardDto> searchBizCards(Long userIdx, String keyword, Pageable pageable);

     // ✅ 복원
    void restoreBizCard(Long id);

    // ✅ 개수
    long countBizCardsByUser(Long userIdx);

    // ✅ 중복확인
    boolean existsBizCard(Long userIdx, String name, String email);

    // ✅ 소프트 삭제된 명함 조회
    Page<BizCardDto> getDeletedBizCardsByUserIdx(Long userIdx, Pageable pageable);

    String storeBizCardImage(byte[] bytes, String filename) throws IOException;
}
