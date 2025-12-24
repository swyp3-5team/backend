package com.moa.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

@Component
@RequiredArgsConstructor
public class DBConnectionInfoLogger {
    private final DataSource dataSource;

    @PostConstruct
    public void logDbInfo() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData meta = connection.getMetaData();
            System.out.println("========== DB INFO ==========");
            System.out.println("URL      : " + meta.getURL());
            System.out.println("User     : " + meta.getUserName());
            System.out.println("Driver   : " + meta.getDriverName());
            System.out.println("=============================");
        }
    }
}
