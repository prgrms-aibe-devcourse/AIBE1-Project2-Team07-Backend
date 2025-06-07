package org.lucky0111.pettalk.repository.user;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lucky0111.pettalk.domain.common.UserRole;
import org.lucky0111.pettalk.domain.entity.user.PetUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
class PetUserRepositoryTest {
    @Autowired
    private PetUserRepository petUserRepository;

    private PetUser testUser;

    @BeforeEach
    void setUp() {
        testUser = PetUser.builder()
                .name("test")
                .email("testuser@example.com")
                .nickname("testNickname")
                .profileImageUrl("http://example.com/profile.jpg")
                .socialId("testSocialId123")
                .provider("KAKAO")
                .build();

        petUserRepository.save(testUser);
    }

    @AfterEach
    void tearDown() {
        petUserRepository.delete(testUser);
    }

    @Test
    @DisplayName("중복된 소셜 ID로 사용자를 저장할 수 없어야 한다.")
    void saveUser_WhenSocialIdAlreadyExists_ThenFail() {
        // Given
        PetUser duplicateUser = PetUser.builder()
                .name("test")
                .email("testuser@example.com")
                .nickname("testNickname2")
                .profileImageUrl("http://example.com/profile.jpg")
                .socialId(testUser.getSocialId())
                .provider("KAKAO")
                .build();


        // When & Then
        assertThatThrownBy(() -> petUserRepository.saveAndFlush(duplicateUser))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("중복된 닉네임으로 사용자를 저장할 수 없어야 한다.")
    void saveUser_WhenNicknameAlreadyExists_ThenFail() {
        // Given
        PetUser duplicateUser = PetUser.builder()
                .name("test")
                .email("testuser@example.com")
                .nickname(testUser.getNickname())
                .profileImageUrl("http://example.com/profile.jpg")
                .socialId("testSocialId123456")
                .provider("KAKAO")
                .build();

        // When & Then
        assertThatThrownBy(() -> petUserRepository.saveAndFlush(duplicateUser))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("닉네임에는 NULL 값이 허용되지 않아야 한다.")
    void saveUser_WhenNicknameIsNull_ThenFail() {
        // Given
        PetUser duplicateUser = PetUser.builder()
                .name("test")
                .email("testuser@example.com")
                .nickname("testNickname2")
                .profileImageUrl("http://example.com/profile.jpg")
                .socialId("testSocialId123456")
                .provider("KAKAO")
                .build();

        duplicateUser.updateNickname(null);

        // When & Then
        assertThatThrownBy(() -> petUserRepository.saveAndFlush(duplicateUser))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("닉네임을 업데이트하면 해당 사용자의 닉네임이 변경되어야 한다.")
    void updateUserNickname_ShouldChangeNickname() {
        // Given
        String newNickname = "updatedNickname";
        testUser.updateNickname(newNickname);

        // When
        PetUser updatedUser = petUserRepository.saveAndFlush(testUser);

        // Then
        assertThat(updatedUser.getNickname(), is(newNickname));
    }

    @Test
    @DisplayName("유저 권한을 업데이트하면 해당 사용자의 권한이 변경되어야 한다.")
    void updateUserRole_ShouldChangeRole() {
        // Given
        UserRole newRole = UserRole.ADMIN;
        testUser.updateRole(newRole);

        // When
        PetUser updatedUser = petUserRepository.saveAndFlush(testUser);

        // Then
        assertThat(updatedUser.getRole(), is(newRole));
    }

    @Test
    @DisplayName("유효한 소셜 ID로 사용자를 조회할 수 있어야 한다.")
    void findUser_BySocialId_ShouldReturnUser() {
        // Given
        String socialId = testUser.getSocialId();

        // When
        PetUser result = petUserRepository.findBySocialId(socialId).orElse(null);

        // Then
        assertNotNull(result);
        assertThat(result.getSocialId(), is(socialId));
    }

    @Test
    @DisplayName("닉네임으로 사용자가 존재하는지 확인할 수 있어야 한다.")
    void existsByNickname_WhenNicknameExists_ShouldReturnTrue() {
        // Given
        String nickname = testUser.getNickname();

        // When
        boolean result = petUserRepository.existsByNickname(nickname);

        // Then
        assertThat(result, is(true));
    }
}