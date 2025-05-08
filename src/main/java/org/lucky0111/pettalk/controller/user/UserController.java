package org.lucky0111.pettalk.controller.user;

import lombok.RequiredArgsConstructor;
import org.lucky0111.pettalk.domain.dto.user.UserResponseDTO;
import org.lucky0111.pettalk.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> getUser() {
        UserResponseDTO userResponseDTO = userService.getUserById();
        return ResponseEntity.ok(userResponseDTO);
    }
}
