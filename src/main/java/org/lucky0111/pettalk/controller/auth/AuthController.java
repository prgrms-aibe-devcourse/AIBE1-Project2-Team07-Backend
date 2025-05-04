package org.lucky0111.pettalk.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucky0111.pettalk.domain.dto.auth.*;
import org.lucky0111.pettalk.domain.dto.user.ProfileUpdateDTO;
import org.lucky0111.pettalk.domain.entity.user.PetUser;
import org.lucky0111.pettalk.exception.CustomException;
import org.lucky0111.pettalk.repository.user.PetUserRepository;
import org.lucky0111.pettalk.service.auth.ResponseService;
import org.lucky0111.pettalk.service.user.UserService;
import org.lucky0111.pettalk.util.auth.JWTUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JWTUtil jwtUtil;
    private final PetUserRepository userRepository;
    private final ResponseService responseService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @GetMapping("/user-info")
    public ResponseEntity<?> getUserInfo(HttpServletRequest request) {
        try {
            // 현재 인증된 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                throw new CustomException("인증 정보가 없습니다.", HttpStatus.UNAUTHORIZED);
            }

            // JWT 토큰에서 사용자 ID 추출
            UUID userId = jwtUtil.getUserId(extractJwtToken(request));

            if (userId == null) {
                throw new CustomException("유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED);
            }

            // 사용자 정보 조회
            PetUser user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

            // 응답 데이터 생성
            Map<String, Object> userData = createUserDataMap(user);

            return responseService.createSuccessResponse(userData, "사용자 정보를 성공적으로 가져왔습니다.");
        } catch (CustomException e) {
            return responseService.createErrorResponse(e.getHttpStatus().name(), e.getMessage());
        } catch (Exception e) {
            log.error("사용자 정보 조회 중 오류 발생: ", e);
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
            OAuthTempTokenDTO tempTokenInfo = jwtUtil.getTempTokenInfo(registrationDTO.tempToken());

            if (tempTokenInfo == null) {
                throw new CustomException("유효하지 않은 임시 토큰입니다.", HttpStatus.BAD_REQUEST);
            }

            // 소셜 ID로 이미 존재하는 사용자 확인
            PetUser existingUser = userRepository.findByProviderAndSocialId(
                    tempTokenInfo.provider(), tempTokenInfo.providerId());

            PetUser petUser;

            if (existingUser != null) {
                // 기존 사용자 정보 업데이트
                petUser = existingUser;
                petUser.setName(registrationDTO.name());
                petUser.setNickname(registrationDTO.nickname());
                petUser.setProfileImageUrl(registrationDTO.profileImageUrl());

                log.info("기존 사용자 정보 업데이트: {}, {}", petUser.getUserId(), petUser.getName());
            } else {
                // 새 사용자 생성
                petUser = new PetUser();
                petUser.setProvider(tempTokenInfo.provider());
                petUser.setSocialId(tempTokenInfo.providerId());
                petUser.setEmail(tempTokenInfo.email());
                petUser.setName(registrationDTO.name());
                petUser.setNickname(registrationDTO.nickname());
                petUser.setProfileImageUrl(registrationDTO.profileImageUrl());
                petUser.setRole("ROLE_USER");
                petUser.setStatus("ACTIVE");

                log.info("새 사용자 정보 생성: {}, {}, {}",
                        petUser.getName(), petUser.getProvider(), petUser.getSocialId());
            }

            userRepository.save(petUser);

            // Generate JWT token pair (access + refresh)
            TokenDTO tokens = jwtUtil.generateTokenPair(petUser);

            // 응답 데이터 생성
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("accessToken", tokens.accessToken());
            responseData.put("refreshToken", tokens.refreshToken());
            responseData.put("expiresIn", tokens.expiresIn());
            responseData.put("user", createUserDataMap(petUser));

            return responseService.createSuccessResponse(responseData, "사용자 등록이 완료되었습니다.");
        } catch (CustomException e) {
            return responseService.createErrorResponse(e.getHttpStatus().name(), e.getMessage());
        } catch (Exception e) {
            log.error("사용자 등록 중 오류 발생: ", e);
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
            log.error("닉네임 확인 중 오류 발생: ", e);
            return responseService.createErrorResponse("SERVER_ERROR", "서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody TokenRequestDTO request) {
        try {
            String refreshToken = request.refreshToken();

            if (refreshToken == null || refreshToken.isBlank()) {
                throw new CustomException("리프레시 토큰이 필요합니다.", HttpStatus.BAD_REQUEST);
            }

            Optional<TokenDTO> optionalTokenDTO = jwtUtil.refreshAccessToken(refreshToken);

            if (optionalTokenDTO.isPresent()) {
                TokenDTO tokenDTO = optionalTokenDTO.get();
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("accessToken", tokenDTO.accessToken());
                responseData.put("refreshToken", tokenDTO.refreshToken());
                responseData.put("expiresIn", tokenDTO.expiresIn());

                return responseService.createSuccessResponse(responseData, "토큰이 갱신되었습니다.");
            } else {
                throw new CustomException("유효하지 않은 리프레시 토큰입니다.", HttpStatus.UNAUTHORIZED);
            }
        } catch (CustomException e) {
            return responseService.createErrorResponse(e.getHttpStatus().name(), e.getMessage());
        } catch (Exception e) {
            log.error("토큰 갱신 중 오류 발생: ", e);
            return responseService.createErrorResponse("SERVER_ERROR", "서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody TokenRequestDTO request) {
        try {
            String refreshToken = request.refreshToken();

            if (refreshToken == null || refreshToken.isBlank()) {
                throw new CustomException("리프레시 토큰이 필요합니다.", HttpStatus.BAD_REQUEST);
            }

            boolean revoked = jwtUtil.revokeRefreshToken(refreshToken);

            if (revoked) {
                return responseService.createSuccessResponse(null, "로그아웃되었습니다.");
            } else {
                throw new CustomException("유효하지 않은 리프레시 토큰입니다.", HttpStatus.UNAUTHORIZED);
            }
        } catch (CustomException e) {
            return responseService.createErrorResponse(e.getHttpStatus().name(), e.getMessage());
        } catch (Exception e) {
            log.error("로그아웃 중 오류 발생: ", e);
            return responseService.createErrorResponse("SERVER_ERROR", "서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdrawUser(HttpServletRequest request) {
        try {
            UUID userId = validateTokenAndGetUserId(request);

            // 사용자 탈퇴 처리 서비스 호출
            boolean withdrawn = userService.withdrawUser(userId);

            if (withdrawn) {
                // 모든 리프레시 토큰 폐기
                jwtUtil.revokeAllUserTokens(userId);

                return responseService.createSuccessResponse(null, "계정이 성공적으로 탈퇴 처리되었습니다.");
            } else {
                throw new CustomException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
            }
        } catch (CustomException e) {
            return responseService.createErrorResponse(e.getHttpStatus().name(), e.getMessage());
        } catch (Exception e) {
            log.error("계정 탈퇴 처리 중 오류 발생: ", e);
            return responseService.createErrorResponse("SERVER_ERROR", "서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(HttpServletRequest request, @RequestBody ProfileUpdateDTO profileUpdateDTO) {
        try {
            UUID userId = validateTokenAndGetUserId(request);

            // 닉네임 중복 확인 (현재 사용자와 동일한 닉네임은 제외)
            if (profileUpdateDTO.nickname() != null && !profileUpdateDTO.nickname().isBlank()) {
                PetUser currentUser = userRepository.findById(userId).orElse(null);
                if (currentUser != null &&
                        !profileUpdateDTO.nickname().equals(currentUser.getNickname()) &&
                        userRepository.existsByNickname(profileUpdateDTO.nickname())) {
                    throw new CustomException("이미 사용 중인 닉네임입니다. 다른 닉네임을 사용해 주세요.", HttpStatus.CONFLICT);
                }
            }

            // 프로필 업데이트 서비스 호출
            PetUser updatedUser = userService.updateProfile(userId, profileUpdateDTO);

            if (updatedUser != null) {
                // 응답 데이터 생성
                Map<String, Object> userData = createUserDataMap(updatedUser);
                return responseService.createSuccessResponse(userData, "프로필 정보가 성공적으로 업데이트되었습니다.");
            } else {
                throw new CustomException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
            }
        } catch (CustomException e) {
            return responseService.createErrorResponse(e.getHttpStatus().name(), e.getMessage());
        } catch (Exception e) {
            log.error("프로필 업데이트 중 오류 발생: ", e);
            return responseService.createErrorResponse("SERVER_ERROR", "서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
        }
    }

    @GetMapping("/token/validate")
    public ResponseEntity<?> validateToken(HttpServletRequest request) {
        try {
            String token = extractJwtToken(request);

            if (token == null) {
                throw new CustomException("토큰을 찾을 수 없습니다.", HttpStatus.BAD_REQUEST);
            }

            // 토큰 유효성 및 만료 시간 확인
            if (jwtUtil.isExpired(token)) {
                throw new CustomException("토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED);
            }

            // 토큰에서 남은 만료 시간 계산 (초 단위)
            long expiresIn = jwtUtil.getExpiresIn(token);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("valid", true);
            responseData.put("expiresIn", expiresIn);

            return responseService.createSuccessResponse(responseData, "토큰이 유효합니다.");

        } catch (CustomException e) {
            return responseService.createErrorResponse(e.getHttpStatus().name(), e.getMessage());
        } catch (Exception e) {
            log.error("토큰 검증 중 오류 발생: ", e);
            return responseService.createErrorResponse("INVALID_TOKEN", "유효하지 않은 토큰입니다.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginWithOAuthData(@RequestBody LoginDTO loginDTO) {
        try {
            String encodedData = loginDTO.encodedData();
            log.debug("Received encoded data: {}", encodedData);

            // URL 디코딩 두 번 수행 (이중 URL 인코딩 처리)
            String urlDecodedData = URLDecoder.decode(encodedData, StandardCharsets.UTF_8);
            urlDecodedData = URLDecoder.decode(urlDecodedData, StandardCharsets.UTF_8);

            log.debug("After URL decoding: {}", urlDecodedData);

            // Base64 디코딩
            byte[] decodedBytes = Base64.getDecoder().decode(urlDecodedData);
            String jsonData = new String(decodedBytes, StandardCharsets.UTF_8);

            log.debug("Decoded JSON: {}", jsonData);

            // JSON 파싱
            Map<String, Object> userData = objectMapper.readValue(jsonData, Map.class);

            // 사용자 정보 추출
            Map<String, Object> userInfo = (Map<String, Object>) userData.get("user");

            if (userInfo == null || userInfo.get("id") == null) {
                throw new CustomException("유효하지 않은 사용자 정보입니다.", HttpStatus.BAD_REQUEST);
            }

            // 사용자 ID를 UUID로 변환
            UUID userId = UUID.fromString(userInfo.get("id").toString());

            // 데이터베이스에서 사용자 조회
            PetUser user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException("존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND));

            // 사용자 상태 확인
            if (!"ACTIVE".equals(user.getStatus())) {
                throw new CustomException("비활성화된 사용자입니다.", HttpStatus.FORBIDDEN);
            }

            // 기존에 제공된 토큰 사용
            String accessToken = (String) userData.get("accessToken");
            String refreshToken = (String) userData.get("refreshToken");
            Long expiresIn = ((Number) userData.get("expiresIn")).longValue();

            // 응답 데이터 생성
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("accessToken", accessToken);
            responseData.put("refreshToken", refreshToken);
            responseData.put("expiresIn", expiresIn);
            responseData.put("user", userInfo);

            return responseService.createSuccessResponse(responseData, "로그인이 성공적으로 처리되었습니다.");

        } catch (CustomException e) {
            return responseService.createErrorResponse(e.getHttpStatus().name(), e.getMessage());
        } catch (Exception e) {
            log.error("로그인 처리 중 오류 발생: ", e);
            return responseService.createErrorResponse("SERVER_ERROR", "서버 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 사용자 정보를 맵으로 변환합니다.
     */
    private Map<String, Object> createUserDataMap(PetUser user) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getUserId());
        userData.put("email", user.getEmail());
        userData.put("name", user.getName());
        userData.put("nickname", user.getNickname());
        userData.put("profileImageUrl", user.getProfileImageUrl());
        userData.put("role", user.getRole());
        return userData;
    }

    /**
     * 토큰을 검증하고 사용자 ID를 반환합니다.
     */
    private UUID validateTokenAndGetUserId(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new CustomException("인증 정보가 없습니다.", HttpStatus.UNAUTHORIZED);
        }

        String token = extractJwtToken(request);
        if (token == null) {
            throw new CustomException("유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED);
        }

        UUID userId = jwtUtil.getUserId(token);
        if (userId == null) {
            throw new CustomException("유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED);
        }

        return userId;
    }
}