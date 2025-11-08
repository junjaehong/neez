package com.bbey.neez.service;

import com.bbey.neez.DTO.BizCardDto;
import com.bbey.neez.entity.BizCard;
import com.bbey.neez.entity.BizCardSaveResult;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface BizCardReaderService {

    /**
     * OCR로 명함 이미지 파일을 읽어서 필드별로 뽑아낸다.
     * @param fileName 리소스/스토리지에 있는 명함 이미지 파일 이름
     * @return company, name, department, position, tel, mobile, fax, email, address 등 키를 가진 Map
     */
    Map<String, String> readBizCard(String fileName);

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
     * 명함 하나를 상세 조회해서
     * 회사명, 메모내용까지 Map으로 내려준다.
     */
    Map<String, Object> getBizCardDetail(Long id);

    /**
     * 명함의 기본 정보만 수정한다.
     */
    BizCard updateBizCard(Long idx, Map<String, String> data);

    /**
     * 명함의 메모만 따로 수정한다.
     * 실제 파일은 MemoStorage에 쓰고, DB에는 파일명만 저장한다.
     */
    BizCard updateBizCardMemo(Long id, String memo);

    /**
     * 명함의 메모 내용만 문자열로 가져온다.
     * @throws IOException 메모 파일을 읽을 수 없을 때
     */
    String getBizCardMemoContent(Long id) throws IOException;

    // BizCardReaderService 에 시그니처 추가
    List<BizCardDto> getBizCardsByUserIdx(Long userIdx);

    void deleteBizCard(Long idx);
    
}
