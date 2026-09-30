package com.example.integrationdemo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "00. 실행 확인", description = "애플리케이션의 HTTP 응답 확인")
public class StatusController {

    @GetMapping("/api/status")
    @Operation(summary = "애플리케이션 실행 확인", description = "DB 및 외부 API 연결 상태는 검사하지 않습니다.")
    public StatusResponse status() {
        return new StatusResponse("integration-api-demo", "UP");
    }

    public record StatusResponse(String application, String status) {
    }
}
