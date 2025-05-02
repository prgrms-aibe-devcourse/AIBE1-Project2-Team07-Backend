package org.lucky0111.pettalk.domain.entity.chat;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.lucky0111.pettalk.domain.common.BaseTimeEntity;
import org.lucky0111.pettalk.domain.entity.PetUser;

import java.util.UUID;

@Data
@Entity
@Table(name = "chatrooms")
@NoArgsConstructor
public class Chatroom extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID chatroomId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private PetUser user;

    @ManyToOne
    @JoinColumn(name = "trainer_id")
    private PetUser trainer;
}
