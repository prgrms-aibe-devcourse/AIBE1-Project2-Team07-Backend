package org.lucky0111.pettalk.domain.entity.chat;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "chats")
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatId;

    @ManyToOne
    @JoinColumn(name = "chatroom_id")
    private Chatroom chatroom;

    @Column(length = 200, nullable = false)
    private String content;
    @Column(nullable = false)
    private String sender;
    @Column(nullable = false)
    private String receiver;

    @CreatedDate
    private LocalDateTime createdAt;
}
