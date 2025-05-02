package org.lucky0111.pettalk.controller.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.lucky0111.pettalk.domain.dto.auth.OAuthTempTokenDTO;
import org.lucky0111.pettalk.domain.dto.auth.TokenDTO;
import org.lucky0111.pettalk.domain.dto.auth.TokenRequest;
import org.lucky0111.pettalk.domain.dto.auth.UserRegistrationDTO;
import org.lucky0111.pettalk.domain.dto.user.ProfileUpdateDTO;
import org.lucky0111.pettalk.domain.entity.PetUser;
import org.lucky0111.pettalk.repository.user.UserRepository;
import org.lucky0111.pettalk.service.auth.ResponseService;
import org.lucky0111.pettalk.service.user.UserService;
import org.lucky0111.pettalk.util.auth.JWTUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = {"${front.url}", "http://localhost:3000"}, maxAge = 3600, allowCredentials = "true")
public class AuthController {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;
    private final ResponseService responseService;
    private final UserService userService;

    public AuthController(JWTUtil jwtUtil, UserRepository userRepository, ResponseService responseService, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.responseService = responseService;
        this.userService = userService;
    }

    @GetMapping("/user-info")
    public ResponseEntity<?> getUserInfo(HttpServletRequest request) {
        try {
            // 현재 인증된 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return responseService.createErrorResponse("UNAUTHORIZED", "인증 정보가 없습니다.");
            }

            // JWT 토큰에서 사용자 ID 추출
            String userId = jwtUtil.getUserId(extractJwtToken(request));

            if (userId == null) {
                return responseService.createErrorResponse("INVALID_TOKEN", "유효하지 않은 토큰입니다.");
            }

            // 사용자 정보 조회
            PetUser user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

            // 응답 데이터 생성
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getUserId());
            userData.put("email", user.getEmail());
            userData.put("name", user.getName());
            userData.put("nickname", user.getNickname());
            userData.put("profileImageUrl", user.getProfileImageUrl());
            userData.put("role", user.getRole());

            return responseService.createSuccessResponse(userData, "사용자 정보를 성공적으로 가져왔습니다.");

        } catch (ResponseStatusException e) {
            return responseService.createErrorResponse("NOT_FOUND", e.getReason());
        } catch (Exception e) {
            System.err.println("사용자 정보 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return responseService.createErrorResponse("SERVER_ERROR", "서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
        }
    }

    // JWT 토큰 추출 헬퍼 메서드
    private String extractJwtToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegistrationDTO registrationDTO) {
        try {
            // 임시 토큰 검증
            OAuthTempTokenDTO tempTokenInfo = jwtUtil.getTempTokenInfo(registrationDTO.getTempToken());

            if (tempTokenInfo == null) {
                return responseService.createErrorResponse("INVALID_TOKEN", "유효하지 않은 임시 토큰입니다.");
            }

            // 소셜 ID로 이미 존재하는 사용자 확인
            PetUser existingUser = userRepository.findByProviderAndSocialId(
                    tempTokenInfo.getProvider(), tempTokenInfo.getProviderId());

            PetUser petUser;

            if (existingUser != null) {
                // 기존 사용자 정보 업데이트
                petUser = existingUser;
                petUser.setName(registrationDTO.getName());
                petUser.setNickname(registrationDTO.getNickname());
                petUser.setProfileImageUrl(registrationDTO.getProfileImageUrl());

                System.out.println("기존 사용자 정보 업데이트: " + petUser.getUserId() + ", " + petUser.getName());
            } else {
                // 새 사용자 생성
                petUser = new PetUser();
                petUser.setProvider(tempTokenInfo.getProvider());
                petUser.setSocialId(tempTokenInfo.getProviderId());
                petUser.setEmail(tempTokenInfo.getEmail());
                petUser.setName(registrationDTO.getName());
                petUser.setNickname(registrationDTO.getNickname());
                petUser.setProfileImageUrl(registrationDTO.getProfileImageUrl());
                petUser.setRole("ROLE_USER");
                petUser.setStatus("ACTIVE");

                System.out.println("새 사용자 정보 생성: " + petUser.getName() + ", " + petUser.getProvider() + ", " + petUser.getSocialId());
            }

            userRepository.save(petUser);

            // Generate JWT token pair (access + refresh)
            TokenDTO tokens = jwtUtil.generateTokenPair(petUser);

            // 응답 데이터 생성
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("accessToken", tokens.getAccessToken());
            responseData.put("refreshToken", tokens.getRefreshToken());
            responseData.put("expiresIn", tokens.getExpiresIn());

            Map<String, Object> userData = new HashMap<>();
            userData.put("id", petUser.getUserId());
            userData.put("email", petUser.getEmail());
            userData.put("name", petUser.getName());
            userData.put("nickname", petUser.getNickname());
            userData.put("profileImageUrl", petUser.getProfileImageUrl());
            userData.put("role", petUser.getRole());

            responseData.put("user", userData);

            return responseService.createSuccessResponse(responseData, "사용자 등록이 완료되었습니다.");

        } catch (DataIntegrityViolationException e) {
            // 중복 키 또는 무결성 제약 조건 위반 처리
            System.err.println("데이터 무결성 위반 오류: " + e.getMessage());
            return responseService.createErrorResponse("DUPLICATE_DATA", "이미 사용 중인 정보가 포함되어 있습니다. 다른 값을 사용해 주세요.");
        } catch (Exception e) {
            // 기타 예외 처리
            System.err.println("사용자 등록 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return responseService.createErrorResponse("SERVER_ERROR", "서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
        }
    }

    @GetMapping("/check-nickname")
    public ResponseEntity<?> checkNickname(@RequestParam String nickname) {
        try {
            // 닉네임 중복 확인 로직 구현
            boolean isAvailable = !userRepository.existsByNickname(nickname);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("available", isAvailable);

            return responseService.createSuccessResponse(responseData,
                    isAvailable ? "사용 가능한 닉네임입니다." : "이미 사용 중인 닉네임입니다.");
        } catch (Exception e) {
            System.err.println("닉네임 확인 중 오류 발생: " + e.getMessage());
            return responseService.createErrorResponse("SERVER_ERROR", "서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody TokenRequest request) {
        try {
            String refreshToken = request.getRefreshToken();

            if (refreshToken == null || refreshToken.isBlank()) {
                return responseService.createErrorResponse("INVALID_REQUEST", "리프레시 토큰이 필요합니다.");
            }

            Optional<TokenDTO> optionalTokenDTO = jwtUtil.refreshAccessToken(refreshToken);

            if (optionalTokenDTO.isPresent()) {
                TokenDTO tokenDTO = optionalTokenDTO.get();
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("accessToken", tokenDTO.getAccessToken());
                responseData.put("refreshToken", tokenDTO.getRefreshToken());
                responseData.put("expiresIn", tokenDTO.getExpiresIn());

                return responseService.createSuccessResponse(responseData, "토큰이 갱신되었습니다.");
            } else {
                return responseService.createErrorResponse("INVALID_TOKEN", "유효하지 않은 리프레시 토큰입니다.");
            }
        } catch (Exception e) {
            System.err.println("토큰 갱신 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return responseService.createErrorResponse("SERVER_ERROR", "서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody TokenRequest request) {
        try {
            String refreshToken = request.getRefreshToken();

            if (refreshToken == null || refreshToken.isBlank()) {
                return responseService.createErrorResponse("INVALID_REQUEST", "리프레시 토큰이 필요합니다.");
            }

            boolean revoked = jwtUtil.revokeRefreshToken(refreshToken);

            if (revoked) {
                return responseService.createSuccessResponse(null, "로그아웃되었습니다.");
            } else {
                return responseService.createErrorResponse("INVALID_TOKEN", "유효하지 않은 리프레시 토큰입니다.");
            }
        } catch (Exception e) {
            System.err.println("로그아웃 중 오류 발생: " + e.getMessage());
            return responseService.createErrorResponse("SERVER_ERROR", "서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
        }
    }

    // 새로 추가된 API 메서드들

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdrawUser(HttpServletRequest request) {
        try {
            // 현재 인증된 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return responseService.createErrorResponse("UNAUTHORIZED", "인증 정보가 없습니다.");
            }

            // JWT 토큰에서 사용자 ID 추출
            String userId = jwtUtil.getUserId(extractJwtToken(request));

            if (userId == null) {
                return responseService.createErrorResponse("INVALID_TOKEN", "유효하지 않은 토큰입니다.");
            }

            // 사용자 탈퇴 처리 서비스 호출
            boolean withdrawn = userService.withdrawUser(userId);

            if (withdrawn) {
                // 모든 리프레시 토큰 폐기
                jwtUtil.revokeAllUserTokens(userId);

                return responseService.createSuccessResponse(null, "계정이 성공적으로 탈퇴 처리되었습니다.");
            } else {
                return responseService.createErrorResponse("NOT_FOUND", "사용자를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            System.err.println("계정 탈퇴 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return responseService.createErrorResponse("SERVER_ERROR", "서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(HttpServletRequest request, @RequestBody ProfileUpdateDTO profileUpdateDTO) {
        try {
            // 현재 인증된 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return responseService.createErrorResponse("UNAUTHORIZED", "인증 정보가 없습니다.");
            }

            // JWT 토큰에서 사용자 ID 추출
            String userId = jwtUtil.getUserId(extractJwtToken(request));

            if (userId == null) {
                return responseService.createErrorResponse("INVALID_TOKEN", "유효하지 않은 토큰입니다.");
            }

            // 닉네임 중복 확인 (현재 사용자와 동일한 닉네임은 제외)
            if (profileUpdateDTO.getNickname() != null && !profileUpdateDTO.getNickname().isBlank()) {
                PetUser currentUser = userRepository.findById(userId).orElse(null);
                if (currentUser != null &&
                        !profileUpdateDTO.getNickname().equals(currentUser.getNickname()) &&
                        userRepository.existsByNickname(profileUpdateDTO.getNickname())) {
                    return responseService.createErrorResponse("DUPLICATE_NICKNAME", "이미 사용 중인 닉네임입니다. 다른 닉네임을 사용해 주세요.");
                }
            }

            // 프로필 업데이트 서비스 호출
            PetUser updatedUser = userService.updateProfile(userId, profileUpdateDTO);

            if (updatedUser != null) {
                // 응답 데이터 생성
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", updatedUser.getUserId());
                userData.put("email", updatedUser.getEmail());
                userData.put("name", updatedUser.getName());
                userData.put("nickname", updatedUser.getNickname());
                userData.put("profileImageUrl", updatedUser.getProfileImageUrl());
                userData.put("role", updatedUser.getRole());

                return responseService.createSuccessResponse(userData, "프로필 정보가 성공적으로 업데이트되었습니다.");
            } else {
                return responseService.createErrorResponse("NOT_FOUND", "사용자를 찾을 수 없습니다.");
            }
        } catch (DataIntegrityViolationException e) {
            return responseService.createErrorResponse("DUPLICATE_DATA", "이미 사용 중인 정보가 포함되어 있습니다. 다른 값을 사용해 주세요.");
        } catch (Exception e) {
            System.err.println("프로필 업데이트 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return responseService.createErrorResponse("SERVER_ERROR", "서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
        }
    }

    @GetMapping("/token/validate")
    public ResponseEntity<?> validateToken(HttpServletRequest request) {
        try {
            String token = extractJwtToken(request);

            if (token == null) {
                return responseService.createErrorResponse("INVALID_REQUEST", "토큰을 찾을 수 없습니다.");
            }

            // 토큰 유효성 및 만료 시간 확인
            if (jwtUtil.isExpired(token)) {
                return responseService.createErrorResponse("TOKEN_EXPIRED", "토큰이 만료되었습니다.");
            }

            // 토큰에서 남은 만료 시간 계산 (초 단위)
            long expiresIn = jwtUtil.getExpiresIn(token);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("valid", true);
            responseData.put("expiresIn", expiresIn);

            return responseService.createSuccessResponse(responseData, "토큰이 유효합니다.");

        } catch (Exception e) {
            System.err.println("토큰 검증 중 오류 발생: " + e.getMessage());
            return responseService.createErrorResponse("INVALID_TOKEN", "유효하지 않은 토큰입니다.");
        }
    }
}