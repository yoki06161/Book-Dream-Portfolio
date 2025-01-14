package com.bookdream.sbb.trade.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findByChatRoomId(Long chatRoomId);
    void deleteByChatRoomId(Long chatRoomId);
}
