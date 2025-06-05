package org.lucky0111.pettalk.repository.user;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lucky0111.pettalk.domain.entity.user.PetUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

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
    @DisplayName("유효한 소셜 ID로 사용자를 조회할 수 있어야 한다.")
    void findBySocialIdTest() {
        testUser = PetUser.builder()
                .name("test")
                .email("testuser@example.com")
                .nickname("testNickname")
                .profileImageUrl("http://example.com/profile.jpg")
                .socialId("testSocialId123")
                .provider("KAKAO")
                .build();

        // Given
        String socialId = testUser.getSocialId();

        // When
        PetUser foundUser = petUserRepository.findBySocialId(socialId).orElse(null);

        // Then
        assertNotNull(foundUser);
        assertThat(foundUser.getSocialId(), is(socialId));
    }

    @Test
    @DisplayName("닉네임으로 사용자가 존재하는지 확인할 수 있어야 한다.")
    void existsByNicknameTest() {
        // Given
        String nickname = testUser.getNickname();

        // When
        boolean exists = petUserRepository.existsByNickname(nickname);

        // Then
        assertThat(exists, is(true));
    }
}