package com.bbey.neez.exception;

import com.bbey.neez.DTO.ApiResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ✅ Bean Validation(@Valid) 에러 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleValidationException(MethodArgumentNotValidException ex) {
        StringBuilder sb = new StringBuilder();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            sb.append(error.getField())
                    .append(": ")
                    .append(error.getDefaultMessage())
                    .append("; ");
        });

        String message = sb.toString();
        if (message.endsWith("; ")) {
            message = message.substring(0, message.length() - 2);
        }

        ApiResponseDto<Object> body = new ApiResponseDto<>(
                false,
                message,
                null);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // ✅ 리소스를 찾을 수 없는 경우 (404)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        ApiResponseDto<Object> body = new ApiResponseDto<>(
                false,
                ex.getMessage(),
                null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // ✅ 권한 없는 접근 (403)
    @ExceptionHandler(AccessDeniedBizException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleAccessDeniedBiz(AccessDeniedBizException ex) {
        ApiResponseDto<Object> body = new ApiResponseDto<>(
                false,
                ex.getMessage(),
                null);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    // ✅ 서비스 단에서 던지는 일반 RuntimeException (400)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleRuntimeException(RuntimeException ex) {
        ApiResponseDto<Object> body = new ApiResponseDto<>(
                false,
                ex.getMessage(),
                null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // ✅ 나머지 예상 못 한 모든 에러 처리 (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Object>> handleException(Exception ex) {
        ApiResponseDto<Object> body = new ApiResponseDto<>(
                false,
                "Unexpected error: " + ex.getMessage(),
                null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
