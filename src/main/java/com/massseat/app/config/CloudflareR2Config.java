package com.massseat.app.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Slf4j
@Configuration
public class CloudflareR2Config {

    private final AppProperties.CloudflareR2Properties r2Properties;

    public CloudflareR2Config(AppProperties appProperties) {
        this.r2Properties = appProperties.getR2Properties();
    }

    @Bean
    public S3Client s3Client() {

        log.info("R2Properties: {}", r2Properties);

        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                r2Properties.getAccessKey(),
                r2Properties.getSecretKey()
        );

        S3Configuration serviceConfiguration = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .chunkedEncodingEnabled(false)
                .build();

        String url =
                "https://"
                        + r2Properties.getAccountId()
                        + ".r2.cloudflarestorage.com";
        return S3Client.builder()
                .endpointOverride(URI.create(url))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of("auto")) // R2 `auto` region
                .serviceConfiguration(serviceConfiguration)
                .build();
    }

}
