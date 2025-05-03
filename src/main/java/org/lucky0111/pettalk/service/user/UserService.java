package org.lucky0111.pettalk.service.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.lucky0111.pettalk.domain.dto.user.ProfileUpdateDTO;
import org.lucky0111.pettalk.domain.entity.user.PetUser;
import org.lucky0111.pettalk.repository.user.PetUserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PetUserRepository userRepository;

    @Transactional
    public boolean withdrawUser(UUID userId) {
        Optional<PetUser> userOptional = userRepository.findById(userId);

        if (userOptional.isPresent()) {
            PetUser user = userOptional.get();
            // 실제 삭제 대신 상태를 변경하는 논리적 삭제 (Soft Delete) 방식 적용
            user.setStatus("WITHDRAWN");
            user.setSocialId(null);
            userRepository.save(user);
            return true;
        }

        return false;
    }

    @Transactional
    public PetUser updateProfile(UUID userId, ProfileUpdateDTO profileUpdateDTO) {
        Optional<PetUser> userOptional = userRepository.findById(userId);

        if (userOptional.isPresent()) {
            PetUser user = userOptional.get();

            // 닉네임 업데이트 (값이 있을 경우에만)
            if (profileUpdateDTO.nickname() != null && !profileUpdateDTO.nickname().isBlank()) {
                user.setNickname(profileUpdateDTO.nickname());
            }

            // 프로필 이미지 URL 업데이트 (값이 있을 경우에만)
            if (profileUpdateDTO.profileImageUrl() != null && !profileUpdateDTO.profileImageUrl().isBlank()) {
                user.setProfileImageUrl(profileUpdateDTO.profileImageUrl());
            }

            return userRepository.save(user);
        }

        return null;
    }
}