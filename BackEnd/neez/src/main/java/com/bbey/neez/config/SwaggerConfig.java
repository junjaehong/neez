package com.bbey.neez.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * springdoc-openapi 설정
 * - /swagger-ui/index.html 에서 확인 가능
 * - Bearer JWT 인증 스키마 등록
 */
@Configuration
@SecurityScheme(name = "BearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class SwaggerConfig {

        @Bean
        public OpenAPI neezOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Neez BizCard API")
                                                .description("명함 OCR / 수기 등록 / 회사 정보 매칭 / 메모 / 해시태그 API 문서")
                                                .version("v1.0.0")
                                                .license(new License().name("Apache 2.0")));
        }
}
