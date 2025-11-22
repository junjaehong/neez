package com.bbey.neez.exception;

/**
 * 리소스를 찾을 수 없을 때 사용하는 공통 예외.
 * 예: BizCard, Company, User 등 조회 실패
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
