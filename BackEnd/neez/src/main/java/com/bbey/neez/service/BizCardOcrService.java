package com.bbey.neez.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface BizCardOcrService {

    // 이미지 파일명 기준으로 OCR 호출
    Map<String, String> readBizCard(String fileName);

    // multipart로 온 이미지 저장
    String storeBizCardImage(MultipartFile file) throws IOException;

    // 바이너리로 온 이미지 저장
    String storeBizCardImage(byte[] bytes, String filename) throws IOException;
}
