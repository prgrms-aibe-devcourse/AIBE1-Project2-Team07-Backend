package org.lucky0111.pettalk.config.auth;

import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Configuration
public class JWTConfig {
    @Getter
    @Value("${spring.jwt.access-token-expiration-ms}")
    private Long accessTokenExpiresIn;

    @Getter
    @Value("${spring.jwt.refresh-token-expiration-days}")
    private Long refreshTokenExpiresInDays;

    @Value("${spring.jwt.secret}")
    private String secret;

    @Bean
    public SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
