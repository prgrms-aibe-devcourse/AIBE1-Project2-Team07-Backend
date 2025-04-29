package org.lucky0111.pettalk.repository.chat;

import org.lucky0111.pettalk.domain.entity.chat.Chatroom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatroomRepository extends JpaRepository<Chatroom, String> {
}
