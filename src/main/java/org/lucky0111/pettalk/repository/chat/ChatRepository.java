package org.lucky0111.pettalk.repository.chat;

import org.lucky0111.pettalk.domain.entity.chat.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<Chat, Long> {
}
