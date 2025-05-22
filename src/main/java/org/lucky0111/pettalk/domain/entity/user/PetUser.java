package org.lucky0111.pettalk.domain.entity.user;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.lucky0111.pettalk.domain.common.BaseTimeEntity;
import org.lucky0111.pettalk.domain.common.UserRole;

import java.util.UUID;

@Getter
@Entity
@Table(name = "pet_users")
@NoArgsConstructor
public class PetUser extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;

    private String name;

    private String email;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(unique = true, nullable = false)
    private String nickname;

    private String profileImageUrl;

    private String provider; // KAKAO, NAVER

    @Column(unique = true)
    private String socialId; // 토큰 > 소셜 ID

    private String status;

    @Builder
    public PetUser(String name, String email, String nickname, String profileImageUrl, String provider, String socialId) {
        this.name = name;
        this.email = email;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.provider = provider;
        this.socialId = socialId;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateRole(UserRole role) {
        this.role = role;
    }

    public void updateStatus(String status) {
        this.status = status;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void updateSocialId(String socialId) {
        this.socialId = socialId;
    }
}


