package org.lucky0111.pettalk.domain.entity.subscription;

import jakarta.persistence.*;
import lombok.Getter;
import org.lucky0111.pettalk.domain.entity.user.PetUser;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Getter
@Entity
@Table(name = "subscription")
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID subscriptionId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private PetUser user;

    @CreatedDate
    private LocalDateTime purchaseDate;
    private LocalDateTime expirationDate;

    @PrePersist
    public void prePersist() {
        if (this.expirationDate == null) {
            this.expirationDate = LocalDateTime.now().plus(31, ChronoUnit.DAYS);

        }

    }
}