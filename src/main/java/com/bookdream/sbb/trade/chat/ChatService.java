package com.bookdream.sbb.trade.chat;

import com.bookdream.sbb.DataNotFoundException;
import com.bookdream.sbb.trade.Trade;
import com.bookdream.sbb.trade.TradeService;
import com.bookdream.sbb.user.Member;
import com.bookdream.sbb.user.MemberService;
import com.bookdream.sbb.user.SiteUser;
import com.bookdream.sbb.user.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final TradeService tradeService;
    private final UserService userService;
    private final MemberService memberService;
    private final SimpMessagingTemplate messagingTemplate;

    private final Map<Long, Set<String>> activeUsers = new ConcurrentHashMap<>();

    public void userJoined(Long chatRoomId, String userId) {
        activeUsers.computeIfAbsent(chatRoomId, k -> ConcurrentHashMap.newKeySet()).add(userId);
    }

    public void userLeft(Long chatRoomId, String userId) {
        Set<String> users = activeUsers.get(chatRoomId);
        if (users != null) {
            users.remove(userId);
            if (users.isEmpty()) {
                activeUsers.remove(chatRoomId);
            }
        }
    }

    public List<Chat> getChatHistory(Long chatRoomId) {
        return chatRepository.findByChatRoomId(chatRoomId);
    }

    public void saveChat(Chat chat) {
        chat.setCreatedAt(LocalDateTime.now());
        chat.setUnreadCount(1);
        Chat savedChat = chatRepository.save(chat);

        updateNewMessagesCount(savedChat.getChatRoomId(), savedChat.getSenderId());

        chatRoomRepository.findById(savedChat.getChatRoomId()).ifPresent(chatRoom -> {
            if (savedChat.getType() == Chat.MessageType.IMAGE) {
                chatRoom.setLastMessage("사진을 보냈습니다.");
            } else {
                chatRoom.setLastMessage(savedChat.getMessage());
            }
            chatRoom.setLastMessageTime(savedChat.getCreatedAt());
            chatRoom.setLastMessageSenderId(savedChat.getSenderId());
            chatRoomRepository.save(chatRoom);

            messagingTemplate.convertAndSend("/topic/chatRoomsUpdate", chatRoom);
        });
    }

    public List<ChatRoom> getChatRooms(String userId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findBySenderIdOrReceiverId(userId, userId);

        return chatRooms.stream().peek(chatRoom -> {
            String opponentId = chatRoom.getOpponentId(userId);

            if (opponentId != null) {
                String opponentUsername = getUserNameByUserId(opponentId);
                chatRoom.setOpponentUsername(opponentUsername);
            } else {
                chatRoom.setOpponentUsername("상대방이 나갔습니다.");
            }
        }).collect(Collectors.toList());
    }

    private String getUserNameByUserId(String userId) {
        String username = "Unknown User";

        // 우선 SiteUser에서 찾기
        try {
            SiteUser siteUser = userService.getUser(userId);
            if (siteUser != null && "site".equals(siteUser.getProvider())) {
                username = siteUser.getUsername();
            }
        } catch (DataNotFoundException e) {
            // SiteUser에서 찾지 못한 경우 Member에서 찾기
            Member member = memberService.getLoginMemberByLoginId(userId);
            if (member != null) {
                username = member.getName();
            }
        }
        return username;
    }

    public ChatRoom createChatRoom(String senderId, String receiverId, int tradeIdx) {
        // 5. orElseGet 내부 변수명 변경하여 중복 해결
        return chatRoomRepository.findBySenderIdAndReceiverIdAndTradeIdx(senderId, receiverId, tradeIdx)
                .orElseGet(() -> {
                    Trade trade = tradeService.getTradeById(tradeIdx);
                    if (trade == null) {
                        throw new DataNotFoundException("거래 정보를 찾을 수 없습니다: " + tradeIdx);
                    }
                    ChatRoom newRoom = new ChatRoom();
                    newRoom.setSenderId(senderId);
                    newRoom.setReceiverId(receiverId);
                    newRoom.setTradeIdx(tradeIdx);
                    newRoom.setBookTitle(trade.getTitle());
                    newRoom.setLastMessageTime(LocalDateTime.now());
                    newRoom.setNewMessagesCountForSender(0);
                    newRoom.setNewMessagesCountForReceiver(0);
                    return chatRoomRepository.save(newRoom);
                });
    }

    @Transactional
    public void leaveChatRoom(Long chatRoomId, String userId) {
        chatRoomRepository.findById(chatRoomId).ifPresent(chatRoom -> {
            boolean isSender = userId.equals(chatRoom.getSenderId());
            boolean isReceiver = userId.equals(chatRoom.getReceiverId());

            if (isSender) {
                chatRoom.setSenderId(null);
            } else if (isReceiver) {
                chatRoom.setReceiverId(null);
            }

            chatRoomRepository.save(chatRoom);

            Chat leaveMessage = new Chat();
            leaveMessage.setSenderId(userId);
            leaveMessage.setMessage("상대방이 나갔습니다.");
            leaveMessage.setChatRoomId(chatRoomId);
            leaveMessage.setType(Chat.MessageType.LEAVE);
            chatRepository.save(leaveMessage);

            messagingTemplate.convertAndSend("/topic/public", leaveMessage);

            if (chatRoom.getSenderId() == null && chatRoom.getReceiverId() == null) {
                chatRepository.deleteByChatRoomId(chatRoomId);
                chatRoomRepository.delete(chatRoom);
            }

            userLeft(chatRoomId, userId);
        });
    }

    public void incrementNewMessagesCount(Long chatRoomId, String senderId, String currentUserId) {
        chatRoomRepository.findById(chatRoomId).ifPresent(chatRoom -> {
            if (!activeUsers.getOrDefault(chatRoomId, ConcurrentHashMap.newKeySet()).contains(chatRoom.getReceiverId())) {
                chatRoom.setNewMessagesCountForReceiver(chatRoom.getNewMessagesCountForReceiver() + 1);
            } else if (!activeUsers.getOrDefault(chatRoomId, ConcurrentHashMap.newKeySet()).contains(chatRoom.getSenderId())) {
                chatRoom.setNewMessagesCountForSender(chatRoom.getNewMessagesCountForSender() + 1);
            }
            chatRoomRepository.save(chatRoom);
        });
    }

    public void resetNewMessagesCount(Long chatRoomId, String userId) {
        chatRoomRepository.findById(chatRoomId).ifPresent(chatRoom -> {
            if (userId.equals(chatRoom.getReceiverId())) {
                chatRoom.setNewMessagesCountForReceiver(0);
            } else {
                chatRoom.setNewMessagesCountForSender(0);
            }
            chatRoomRepository.save(chatRoom);
        });
    }

    private void updateNewMessagesCount(Long chatRoomId, String senderId) {
        chatRoomRepository.findById(chatRoomId).ifPresent(chatRoom -> {
            if (!activeUsers.getOrDefault(chatRoomId, ConcurrentHashMap.newKeySet()).contains(chatRoom.getReceiverId())) {
                chatRoom.setNewMessagesCountForReceiver(chatRoom.getNewMessagesCountForReceiver() + 1);
            } else if (!activeUsers.getOrDefault(chatRoomId, ConcurrentHashMap.newKeySet()).contains(chatRoom.getSenderId())) {
                chatRoom.setNewMessagesCountForSender(chatRoom.getNewMessagesCountForSender() + 1);
            }
            chatRoomRepository.save(chatRoom);
        });
    }

    public int getTotalNewMessagesCount(String userId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findBySenderIdOrReceiverId(userId, userId);
        return chatRooms.stream()
                        .mapToInt(chatRoom -> chatRoom.getNewMessagesCountForUser(userId))
                        .sum();
    }

    public void sendNewMessagesCount(String userId) {
        int totalNewMessagesCount = getTotalNewMessagesCount(userId);
        messagingTemplate.convertAndSendToUser(userId, "/queue/newMessagesCount", totalNewMessagesCount);
    }

    @Transactional
    public void markMessageAsRead(Long messageId, String readerId) {
        chatRepository.findById(messageId).ifPresent(chat -> {
            if (chat.getUnreadCount() > 0 && !chat.getSenderId().equals(readerId)) {
                chat.setUnreadCount(chat.getUnreadCount() - 1);
                chatRepository.save(chat);
            }
        });
    }

    @Transactional
    public void markMessagesAsRead(Long chatRoomId, String userId) {
        List<Chat> chats = chatRepository.findByChatRoomId(chatRoomId);
        for (Chat chat : chats) {
            if (!chat.getSenderId().equals(userId) && chat.getUnreadCount() > 0) {
                chat.setUnreadCount(chat.getUnreadCount() - 1);
                chatRepository.save(chat);
            }
        }
        // 실시간으로 모든 클라이언트에게 업데이트된 채팅 메시지를 전송
        messagingTemplate.convertAndSend("/topic/chatRoomsUpdate", chatRoomId);
    }
}
