package com.example.integrationdemo.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI integrationDemoOpenApi() {
        return new OpenAPI().info(new Info()
                .title("도서 검색 및 외부 API 연동 데모")
                .version("0.3.0")
                .description("Java / Spring Boot 백엔드 포트폴리오. "
                        + "카카오 도서 검색 API를 호출하고 외부 응답을 서비스 DTO로 변환합니다. "
                        + "ISBN으로 조회한 도서를 PostgreSQL에 저장하고 즐겨찾기 목록·삭제 API를 제공합니다."));
    }
}
