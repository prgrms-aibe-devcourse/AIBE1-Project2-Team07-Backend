package org.lucky0111.pettalk.config.auth;

import org.lucky0111.pettalk.util.auth.JWTUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class TokenCleanupScheduler {

    private final JWTUtil jwtUtil;

    public TokenCleanupScheduler(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // Run every day at 2 AM
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredTokens() {
        System.out.println("Running scheduled task to clean up expired refresh tokens");
        jwtUtil.removeExpiredTokens();
    }
}