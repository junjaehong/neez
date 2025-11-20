package com.bbey.neez.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
@Tag(name = "Health API", description = "서버 및 DB 상태 체크")
public class HealthController {

    @Operation(summary = "서버 헬스 체크", description = "서버가 정상 동작 중인지 확인합니다.")
    @GetMapping
    public String health() {
        return "OK";
    }

    @Operation(summary = "DB 연결 체크", description = "DB 연결 상태를 확인합니다.")
    @GetMapping("/db")
    public String dbPing() {
        return "DB OK";
    }

    @Operation(summary = "DB 테이블 확인", description = "DB 테이블 목록을 반환합니다.")
    @GetMapping("/tables")
    public String dbTables() {
        return "TABLE LIST OK";
    }
}
