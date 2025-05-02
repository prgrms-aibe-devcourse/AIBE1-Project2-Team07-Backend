package org.lucky0111.pettalk.domain.entity.auth;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lucky0111.pettalk.domain.common.BaseTimeEntity;
import org.lucky0111.pettalk.domain.entity.user.PetUser;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "refresh_tokens")
@NoArgsConstructor
public class RefreshToken extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String token;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private PetUser user;

    private LocalDateTime expiryDate;

    private boolean revoked;

    // Constructor for convenience
    public RefreshToken(String token, PetUser user, LocalDateTime expiryDate) {
        this.token = token;
        this.user = user;
        this.expiryDate = expiryDate;
        this.revoked = false;
    }

    public boolean isExpired() {
        return expiryDate.isBefore(LocalDateTime.now());
    }

    public boolean isValid() {
        return !isExpired() && !revoked;
    }
}