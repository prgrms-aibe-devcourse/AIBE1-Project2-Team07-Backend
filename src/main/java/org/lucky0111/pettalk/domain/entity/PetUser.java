package org.lucky0111.pettalk.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lucky0111.pettalk.domain.common.BaseTimeEntity;

@Setter
@Getter
@Entity
@Table(name = "pet_users")
@NoArgsConstructor
public class PetUser extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
//    @Column(length = 36)
    private String userId;

    private String name;

    private String email;

    private String role;

    @Column(unique = true)
    private String nickname;

    private String profileImageUrl;

    private String provider; // KAKAO, NAVER

    @Column(unique = true)
    private String socialId; // 토큰 > 소셜 ID

    private String status;
}


