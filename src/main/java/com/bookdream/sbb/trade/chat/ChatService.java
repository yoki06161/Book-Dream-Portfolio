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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    @Transactional
    public void saveChat(Chat chat) {
        chat.setCreatedAt(LocalDateTime.now());

        // 채팅방 정보를 조회하여 상대방 ID를 찾습니다.
        chatRoomRepository.findById(chat.getChatRoomId()).ifPresent(chatRoom -> {
            String senderId = chat.getSenderId();
            String recipientId = chatRoom.getSenderId().equals(senderId) ? chatRoom.getReceiverId() : chatRoom.getSenderId();

            // 현재 채팅방에 접속 중인 사용자 목록을 가져옵니다.
            Set<String> usersInRoom = activeUsers.get(chat.getChatRoomId());
            boolean isRecipientActive = usersInRoom != null && usersInRoom.contains(recipientId);

            // 상대방의 접속 상태에 따라 '안 읽음' 카운트를 설정합니다.
            if (isRecipientActive) {
                chat.setUnreadCount(0); // 상대방이 접속 중이면 '읽음'으로 처리
            } else {
                chat.setUnreadCount(1); // 상대방이 없으면 '안 읽음'으로 처리
            }

            Chat savedChat = chatRepository.save(chat);

            // 채팅방 목록에 표시될 마지막 메시지 정보를 업데이트합니다.
            if (savedChat.getType() == Chat.MessageType.IMAGE) {
                chatRoom.setLastMessage("사진을 보냈습니다.");
            } else {
                chatRoom.setLastMessage(savedChat.getMessage());
            }
            chatRoom.setLastMessageTime(savedChat.getCreatedAt());
            chatRoom.setLastMessageSenderId(savedChat.getSenderId());

            // 상대방이 접속 중이 아닐 때만, 채팅방의 전체 안 읽은 메시지 수를 증가시킵니다.
            if (!isRecipientActive) {
                if (recipientId.equals(chatRoom.getReceiverId())) {
                    chatRoom.setNewMessagesCountForReceiver(chatRoom.getNewMessagesCountForReceiver() + 1);
                } else {
                    chatRoom.setNewMessagesCountForSender(chatRoom.getNewMessagesCountForSender() + 1);
                }
            }

            chatRoomRepository.save(chatRoom);
            messagingTemplate.convertAndSend("/topic/chatRoomsUpdate", chatRoom);

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    // 이 블록 안의 코드는 DB 저장이 완전히 완료된 후에 실행됩니다.
                    sendNewMessagesCount(senderId);   // 메시지 보낸 사람의 배지 업데이트
                    sendNewMessagesCount(recipientId); // 메시지 받는 사람의 배지 업데이트
                }
            });
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
    public Chat markMessageAsRead(Long messageId, String readerId) {
        // ID로 메시지를 찾습니다.
        Optional<Chat> optionalChat = chatRepository.findById(messageId);

        // 메시지가 존재하면 '읽음' 처리 로직을 실행합니다.
        if (optionalChat.isPresent()) {
            Chat chat = optionalChat.get();
            // 메시지가 아직 안 읽혔고, 보낸 사람이 아닌 경우에만 처리합니다.
            if (chat.getUnreadCount() > 0 && !chat.getSenderId().equals(readerId)) {
                chat.setUnreadCount(0); // 안 읽은 사람 수를 0으로 설정합니다.
                return chatRepository.save(chat); // 변경된 내용을 저장하고 반환합니다.
            }
            return chat; // 이미 읽었거나 자신의 메시지면 변경 없이 그대로 반환합니다.
        }
        return null; // 메시지가 없으면 null을 반환합니다.
    }

    @Transactional
    public void markMessagesAsRead(Long chatRoomId, String userId) {
        List<Chat> chats = chatRepository.findByChatRoomId(chatRoomId);
        for (Chat chat : chats) {
            if (!chat.getSenderId().equals(userId) && chat.getUnreadCount() > 0) {
                chat.setUnreadCount(0); // 안 읽은 메시지 수를 0으로 변경
                chatRepository.save(chat);
            }
        }
        // 클라이언트에게 UI 갱신이 필요함을 알립니다.
        messagingTemplate.convertAndSend("/topic/chatRoomsUpdate", chatRoomId);
    }


}
