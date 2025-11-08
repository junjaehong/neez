package com.bbey.neez.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI neezOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Neez BizCard API")
                        .description("명함 OCR/수기 등록/조회 API 문서")
                        .version("v1.0")
                        .license(new License().name("Apache 2.0")))
                .externalDocs(new ExternalDocumentation()
                        .description("Neez GitHub")
                        .url("https://example.com"));
    }
}

