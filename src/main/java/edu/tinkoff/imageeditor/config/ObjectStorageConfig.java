package edu.tinkoff.imageeditor.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectStorageConfig {

    @Bean
    public MinioClient minioClient(
            @Value("${minio.datasource.url}") final String endpoint,
            @Value("${minio.datasource.username}") final String accessKey,
            @Value("${minio.datasource.password}") final String secretKey
    ) {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}

