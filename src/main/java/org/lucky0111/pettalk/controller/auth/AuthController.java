package org.lucky0111.pettalk.controller.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucky0111.pettalk.domain.dto.auth.TokenDTO;
import org.lucky0111.pettalk.exception.CustomException;
import org.lucky0111.pettalk.service.auth.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(@RequestBody Map<String, String> token) {
        String refreshToken = token.get("refreshToken");
        TokenDTO tokenDTO = authService.reissue(refreshToken);
        return ResponseEntity.ok(tokenDTO);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization") String accessToken) {
        accessToken = getValidToken(accessToken);
        TokenDTO tokenDTO = authService.logout(accessToken);
        return ResponseEntity.ok(tokenDTO);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw() {

        return ResponseEntity.ok().build();
    }

    private String getValidToken(String accessToken) {
        if (!isValidAccessToken(accessToken)) {
            throw new CustomException("토큰이 유효하지 않습니다.", HttpStatus.BAD_REQUEST);
        }
        return extractAccessToken(accessToken);
    }

    private boolean isValidAccessToken(String accessToken) {
        return accessToken != null && accessToken.startsWith("Bearer ");
    }

    private String extractAccessToken(String accessToken) {
        return accessToken.substring(7);
    }
}