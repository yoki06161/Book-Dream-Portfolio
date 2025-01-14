package com.bookdream.sbb.trade.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    List<ChatRoom> findBySenderIdOrReceiverId(String senderId, String receiverId);
    Optional<ChatRoom> findBySenderIdAndReceiverIdAndTradeIdx(String senderId, String receiverId, int tradeIdx);
}
