package org.lucky0111.pettalk.service.user;

import org.lucky0111.pettalk.domain.dto.user.UserRegisterDTO;
import org.lucky0111.pettalk.domain.dto.user.UserResponseDTO;
import org.lucky0111.pettalk.domain.dto.user.UserUpdateDTO;
import org.lucky0111.pettalk.domain.entity.user.PetUser;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

public interface UserService {
    boolean withdrawUser(UUID userId);

    PetUser updateUserProfile(UserRegisterDTO userRegisterDTO, Authentication authentication);

    UserResponseDTO getUserById();

    UserUpdateDTO updateUser(UserUpdateDTO requestDTO);

    UserUpdateDTO updateUserImage(MultipartFile image) throws IOException;
}