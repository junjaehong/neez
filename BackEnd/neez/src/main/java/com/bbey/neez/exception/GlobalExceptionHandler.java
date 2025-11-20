package com.bbey.neez.exception;

import com.bbey.neez.DTO.ApiResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@RestControllerAdvice(basePackages = "com.bbey.neez.controller")
public class GlobalExceptionHandler {

    /**
     * 공통 응답 빌더
     */
    private ResponseEntity<ApiResponseDto<Object>> buildResponse(HttpStatus status, String message, Object data) {
        ApiResponseDto<Object> body = new ApiResponseDto<>(
                false,
                message,
                data
        );
        return ResponseEntity.status(status).body(body);
    }

    /**
     * @Valid 검증 실패 (DTO 필드 validation)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        log.warn("Validation error: {}", errors);
        return buildResponse(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다.", errors);
    }

    /**
     * JSON 파싱 실패 / request body 형식 오류
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("HttpMessageNotReadableException: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "요청 본문을 읽을 수 없습니다.", null);
    }

    /**
     * 지원하지 않는 HTTP 메서드
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.warn("Method not supported: {}", ex.getMessage());
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다.", null);
    }

    /**
     * 잘못된 인자 (서비스/도메인에서 IllegalArgumentException 던졌을 때)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("IllegalArgumentException: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    /**
     * 조회 대상 없음 (ex: findById().orElseThrow(...))
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleNoSuchElement(NoSuchElementException ex) {
        log.warn("NoSuchElementException: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    /**
     * 그 외 처리되지 않은 모든 예외 (마지막 방어선)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Object>> handleException(Exception ex) {
        log.error("Unexpected error", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.", null);
    }
}
