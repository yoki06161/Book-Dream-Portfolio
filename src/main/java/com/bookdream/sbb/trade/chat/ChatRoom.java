package com.bookdream.sbb.trade.chat;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import com.bookdream.sbb.util.DateUtils;

@Entity
@Getter
@Setter
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String senderId;
    private String receiverId;
    private int tradeIdx;
    private String bookTitle;
    private LocalDateTime lastMessageTime;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int newMessagesCountForSender = 0;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int newMessagesCountForReceiver = 0;

    private String opponentUsername;

    private String lastMessageSenderId;

    @Column(columnDefinition = "TEXT")
    private String lastMessage;  // 마지막 메시지 내용

    public String getChatRoomName() {
        return senderId + " & " + receiverId + " & " + tradeIdx;
    }

    public void incrementNewMessagesCount(String senderId) {
        if (senderId.equals(this.senderId)) {
            this.newMessagesCountForReceiver++;
        } else {
            this.newMessagesCountForSender++;
        }
        this.lastMessageSenderId = senderId;
    }

    public void resetNewMessagesCount(String userId) {
        if (userId.equals(this.senderId)) {
            this.newMessagesCountForReceiver = 0;
        } else {
            this.newMessagesCountForSender = 0;
        }
    }

    public int getNewMessagesCountForUser(String userId) {
        if (userId.equals(this.receiverId)) {
            return newMessagesCountForReceiver;
        } else {
            return newMessagesCountForSender;
        }
    }

    public String getFormattedLastMessageTime() {
        return DateUtils.formatDateTime(lastMessageTime);
    }

    public String getOpponentId(String userId) {
        return userId.equals(this.senderId) ? this.receiverId : this.senderId;
    }
}