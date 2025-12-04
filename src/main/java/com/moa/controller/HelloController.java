package com.moa.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Hello", description = "테스트용 Hello World API")
@RestController
public class HelloController {

    @Operation(summary = "기본 인사", description = "루트 경로에서 Hello World를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 메시지 반환")
    })
    @GetMapping("/")
    public String hello() {
        return "Hello World!";
    }

    @Operation(summary = "API 인사", description = "API 경로에서 Hello World 메시지를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 메시지 반환")
    })
    @GetMapping("/api/hello")
    public String apiHello() {
        return "Hello World from Moa API!";
    }
}
