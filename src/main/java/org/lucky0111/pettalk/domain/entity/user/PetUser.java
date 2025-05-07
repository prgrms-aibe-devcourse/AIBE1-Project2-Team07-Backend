package org.lucky0111.pettalk.domain.entity.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lucky0111.pettalk.domain.common.BaseTimeEntity;
import org.lucky0111.pettalk.domain.common.UserRole;

import java.util.UUID;


@Setter
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

    @Column(unique = true)
    private String nickname;

    private String profileImageUrl;

    private String provider; // KAKAO, NAVER

    @Column(unique = true)
    private String socialId; // 토큰 > 소셜 ID

    private String status;
}


