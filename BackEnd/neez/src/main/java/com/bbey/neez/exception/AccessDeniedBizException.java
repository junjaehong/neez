package com.bbey.neez.exception;

/**
 * 비즈니스 도메인에서 권한이 없을 때 사용하는 예외.
 * (Spring Security의 AccessDeniedException 과는 별도로, 도메인 레벨에서 사용)
 */
public class AccessDeniedBizException extends RuntimeException {

    public AccessDeniedBizException(String message) {
        super(message);
    }
}
