package com.moa;

import org.springframework.ai.autoconfigure.vectorstore.pgvector.PgVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication(exclude = {
        PgVectorStoreAutoConfiguration.class
})
public class MoaApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoaApplication.class, args);
    }
}
