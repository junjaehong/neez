package com.bbey.neez.component;

import java.io.IOException;

public interface MemoStorage {

    /**
     * 이미 존재하는 파일명에 content 저장 (덮어쓰기)
     */
    void write(String fileName, String content) throws IOException;

    /**
     * 파일 읽기 (없으면 빈 문자열 반환)
     */
    String read(String fileName) throws IOException;

    /**
     * 파일 삭제
     */
    void delete(String fileName) throws IOException;

    /**
     * prefix 기반으로 새로운 파일명을 생성하여 내용 저장
     * 예) prefix = "meeting-" → meeting-20250210T130501.txt
     *
     * @return 생성된 파일명
     */
    String save(String prefix, String content) throws IOException;
}
