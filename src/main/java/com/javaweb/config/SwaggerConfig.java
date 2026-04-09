package com.javaweb.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components().addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()))
                .info(new Info()
                        .title("Hệ Thống API E-commerce Thể Thao")
                        .description("Tài liệu hướng dẫn sử dụng API Backend đồ án Hệ Thống Thương Mại Điện Tử. Tác giả: Tiến Anh.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Tiến Anh")
                                .email("admin@tienanh.com")
                                .url("https://github.com/DACN-OnlineSportsMerchandiseSalesSystem/Backend"))
                );
    }
}
