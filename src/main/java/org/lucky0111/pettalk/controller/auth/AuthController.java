package org.lucky0111.pettalk.controller.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.lucky0111.pettalk.domain.common.TokenType;
import org.lucky0111.pettalk.domain.dto.user.UserRegisterDTO;
import org.lucky0111.pettalk.domain.dto.user.UserResponseDTO;
import org.lucky0111.pettalk.domain.entity.user.PetUser;
import org.lucky0111.pettalk.service.auth.JwtTokenProvider;
import org.lucky0111.pettalk.service.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.CredentialExpiredException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final JwtTokenProvider tokenService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegisterDTO userSignUpRequestDTO,
                                          HttpServletResponse response,
                                          Authentication authentication) {

        return null;
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {

        return ResponseEntity.ok().build();
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw() {

        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(HttpServletRequest request) throws BadRequestException, CredentialExpiredException {
        Cookie[] cookies = request.getCookies();
        String accessToken = tokenService.extractTokenFromCookies(cookies, TokenType.ACCESS);
        String refreshToken = tokenService.extractTokenFromCookies(cookies, TokenType.REFRESH);

        UUID userId = tokenService.getUserId(refreshToken);
        PetUser petUser = userService.getUserById(userId);

        if (accessToken == null || accessToken.isEmpty()) {
            accessToken = tokenService.reissue(petUser, refreshToken);
        }

        UserResponseDTO.UserInfo userInfo = new UserResponseDTO.UserInfo(
                petUser.getName(), petUser.getNickname(), petUser.getEmail(), petUser.getProfileImageUrl());
        UserResponseDTO.TokenInfo tokenInfo = new UserResponseDTO.TokenInfo(accessToken, refreshToken);
        UserResponseDTO userResponseDTO = new UserResponseDTO(userInfo, tokenInfo);

        return ResponseEntity.ok(userResponseDTO);
    }

    // TODO: Controller Advice로 분리 필요
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequestException(BadRequestException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    }

    // TODO: Controller Advice로 분리 필요
    @ExceptionHandler(CredentialExpiredException.class)
    public ResponseEntity<?> handleCredentialExpiredException(CredentialExpiredException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }
}