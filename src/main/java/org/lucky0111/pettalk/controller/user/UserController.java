package org.lucky0111.pettalk.controller.user;

import lombok.RequiredArgsConstructor;
import org.lucky0111.pettalk.domain.dto.user.UserResponseDTO;
import org.lucky0111.pettalk.domain.dto.user.UserUpdateDTO;
import org.lucky0111.pettalk.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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

    @PutMapping("/update")
    public ResponseEntity<UserUpdateDTO> updateUser(
            @RequestBody UserUpdateDTO requestDTO
    ) {
        UserUpdateDTO userUpdateDTO = userService.updateUser(requestDTO);
        return ResponseEntity.ok(userUpdateDTO);
    }

    @PutMapping("/updateImage")
    public ResponseEntity<UserUpdateDTO> updateUserImage(
            @RequestParam("file") MultipartFile image
    ) throws IOException {
        UserUpdateDTO userUpdateDTO = userService.updateUserImage(image);
        return ResponseEntity.ok(userUpdateDTO);
    }
}
