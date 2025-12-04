package com.moa.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI moaOpenAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("로컬 개발 서버");

        Server testServer = new Server();
        testServer.setUrl("http://49.50.133.51:8080");
        testServer.setDescription("NCP 테스트 서버");

        Info info = new Info()
                .title("Moa App API")
                .version("1.0.0")
                .description("Moa App 백엔드 Swagger API 문서");

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer, testServer));
    }
}
