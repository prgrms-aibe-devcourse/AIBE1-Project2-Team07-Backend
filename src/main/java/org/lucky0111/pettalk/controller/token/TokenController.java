package org.lucky0111.pettalk.controller.token;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.lucky0111.pettalk.domain.dto.auth.TokenDTO;
import org.lucky0111.pettalk.service.auth.JwtTokenProvider;
import org.lucky0111.pettalk.service.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.CredentialExpiredException;

@RestController
@RequestMapping("/api/v1/token")
@RequiredArgsConstructor
public class TokenController {
    private final UserService userService;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(@RequestBody TokenDTO tokens, HttpServletRequest request) throws BadRequestException, CredentialExpiredException {


        return ResponseEntity.ok(null);
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
