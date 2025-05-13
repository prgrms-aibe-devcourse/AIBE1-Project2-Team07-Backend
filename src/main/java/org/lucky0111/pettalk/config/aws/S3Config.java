package org.lucky0111.pettalk.config.aws;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class S3Config {

    @Value("${CLOUD.AWS.CREDENTIALS.ACCESS-KEY:}")
    private String accessKey;

    @Value("${CLOUD.AWS.CREDENTIALS.SECRET-KEY:}")
    private String secretKey;

    @Value("${CLOUD.AWS.REGION.STATIC}")
    private String region;

    // LightSail 객체 스토리지 엔드포인트 설정은 필요 없습니다.
    @Value("${CLOUD.AWS.LIGHTSAIL.OBJECT-STORAGE.ENDPOINT}")
    private String lightsailEndpoint;

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        return StaticCredentialsProvider.create(awsCredentials);
    }

    @Bean
    public S3Client s3Client(AwsCredentialsProvider awsCredentialsProvider) {
        S3Client client = S3Client.builder()
                .region(Region.of(region))
//                .endpointOverride(URI.create(lightsailEndpoint))
                .credentialsProvider(awsCredentialsProvider)
//                .forcePathStyle(true) // Path Style 강제
                .build();


        return client; // 생성된 S3Client 반환
    }
}