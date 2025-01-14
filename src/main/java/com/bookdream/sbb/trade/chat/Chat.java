package com.bookdream.sbb.trade.chat;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String senderId;

    @Column(columnDefinition = "LONGTEXT")
    private String message;
    private LocalDateTime createdAt;
    private Long chatRoomId;
    private MessageType type;
    private int unreadCount;
    
    public enum MessageType {
        CHAT,
        IMAGE,
        JOIN,
        LEAVE
    }
}
