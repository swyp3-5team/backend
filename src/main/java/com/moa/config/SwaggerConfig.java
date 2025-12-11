package com.moa.config;

import com.moa.annotation.CurrentUserId;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    static {
        SpringDocUtils.getConfig().addAnnotationsToIgnore(CurrentUserId.class);
    }

    @Bean
    public OpenAPI moaOpenAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("로컬 개발 서버");

        Server domainServerHttp = new Server();
        domainServerHttp.setUrl("http://cloverly.site:8080");
        domainServerHttp.setDescription("클로버리 서버 도메인");

        Server testServer = new Server();
        testServer.setUrl("http://49.50.133.51:8080");
        testServer.setDescription("NCP 테스트 서버 (IP)");

        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        SecurityRequirement securityRequirement = new SecurityRequirement().addList("BearerAuth");

        Info info = new Info()
                .title("Moa App API")
                .version("1.0.0")
                .description("Moa App 백엔드 Swagger API 문서");

        return new OpenAPI()
                .info(info)
                .servers(List.of(domainServerHttp, localServer, testServer))
                .addSecurityItem(securityRequirement)
                .schemaRequirement("BearerAuth", securityScheme)
                ;
    }
}
