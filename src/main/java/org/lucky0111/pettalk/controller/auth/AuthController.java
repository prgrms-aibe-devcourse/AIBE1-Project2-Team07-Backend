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
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegistrationDTO registrationDTO) {

        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {

        return ResponseEntity.ok().build();
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdrawUser() {

        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginWithOAuthData(@RequestBody LoginDTO loginDTO) {

        return ResponseEntity.ok().build();
    }
}