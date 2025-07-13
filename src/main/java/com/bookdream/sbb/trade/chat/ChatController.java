package com.bookdream.sbb.trade.chat;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/trade/chat")
@RequiredArgsConstructor
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/history")
    @ResponseBody
    public ResponseEntity<List<Chat>> getChatHistory(@RequestParam("chatRoomId") Long chatRoomId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(403).build();
        }
        List<Chat> chatHistory = chatService.getChatHistory(chatRoomId);
        return ResponseEntity.ok(chatHistory);
    }

    @GetMapping("/start")
    public String chatPage(@RequestParam("tradeIdx") int tradeIdx, @RequestParam("chatRoomId") Long chatRoomId, Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/user/login";
        }

        String senderId = principal.getName();
        model.addAttribute("senderId", senderId);
        model.addAttribute("tradeIdx", tradeIdx);
        model.addAttribute("chatRoomId", chatRoomId);

        // 새로운 메시지 수 초기화
        chatService.resetNewMessagesCount(chatRoomId, senderId);

        return "trade/chat";
    }

    @GetMapping("/rooms")
    public String chatRooms(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/user/login";
        }

        String userId = principal.getName();
        List<ChatRoom> chatRooms = chatService.getChatRooms(userId);
        model.addAttribute("chatRooms", chatRooms);
        model.addAttribute("userId", userId);
        return "trade/chat_rooms";
    }

    @PostMapping("/create")
    public String createChatRoom(@RequestParam("receiverId") String receiverId,
                                 @RequestParam("tradeIdx") int tradeIdx,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/user/login";
        }

        String senderId = principal.getName();
        ChatRoom chatRoom = chatService.createChatRoom(senderId, receiverId, tradeIdx);

        redirectAttributes.addAttribute("tradeIdx", tradeIdx);
        redirectAttributes.addAttribute("chatRoomId", chatRoom.getId());

        return "redirect:/trade/chat/start";
    }

    @PostMapping("/leave")
    @ResponseBody
    public ResponseEntity<String> leaveChatRoom(@RequestParam("chatRoomId") Long chatRoomId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(403).build();
        }

        String userId = principal.getName();
        chatService.leaveChatRoom(chatRoomId, userId);
        return ResponseEntity.ok("채팅방을 나갔습니다.");
    }

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public Chat sendMessage(Chat chatMessage, Principal principal) throws InterruptedException {
        if (chatMessage.getType() == Chat.MessageType.IMAGE) {
            Thread.sleep(1000); // 이미지 메시지 딜레이 (선택 사항)
        }

        // 서비스의 saveChat을 호출하여 '안 읽음' 카운트 등 모든 상태를 결정합니다.
        chatService.saveChat(chatMessage);

        // 서버에서 모든 상태가 결정된 chatMessage 객체를 클라이언트로 반환합니다.
        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public Chat addUser(Chat chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();

        // 2. null이 아닌지 확인 후 put 호출
        if (sessionAttributes != null) {
            sessionAttributes.put("username", chatMessage.getSenderId());
        } else {
            logger.warn("WebSocket session attributes not found for user: {}", chatMessage.getSenderId());
        }
        return chatMessage;
    }

    @MessageMapping("/chat.userJoined")
    public void userJoined(@RequestBody Map<String, String> payload) {
        Long chatRoomId = Long.parseLong(payload.get("chatRoomId"));
        String userId = payload.get("userId");
        chatService.userJoined(chatRoomId, userId);

        // 해당 채팅방의 메시지들에 대해 읽음 처리
        chatService.markMessagesAsRead(chatRoomId, userId);
        messagingTemplate.convertAndSend("/topic/chatRoomsUpdate", payload);
    }

    @MessageMapping("/chat.userLeft")
    public void userLeft(@RequestBody Map<String, String> payload) {
        Long chatRoomId = Long.parseLong(payload.get("chatRoomId"));
        String userId = payload.get("userId");
        chatService.userLeft(chatRoomId, userId);
    }

    @GetMapping("/newMessagesCount")
    @ResponseBody
    public ResponseEntity<Map<String, Integer>> getNewMessagesCount(Principal principal) {
        if (principal == null) {
            // 로그인하지 않은 경우 0을 반환합니다.
            return ResponseEntity.ok(Map.of("count", 0));
        }
        int newMessagesCount = chatService.getTotalNewMessagesCount(principal.getName());
        // {"count": 숫자} 형태의 JSON 객체로 응답을 생성합니다.
        return ResponseEntity.ok(Map.of("count", newMessagesCount));
    }

    @MessageMapping("/chat.readMessage")
    @SendTo("/topic/public")
    public Chat readMessage(@RequestBody Map<String, Object> payload, Principal principal) {
        if (principal == null) {
            return null;
        }
        try {
            Long messageId = Long.parseLong(String.valueOf(payload.get("id")));
            String readerId = principal.getName(); // 현재 로그인한 사용자 ID를 가져옵니다.
            // 서비스 호출 결과를 그대로 반환하여 @SendTo로 전송합니다.
            return chatService.markMessageAsRead(messageId, readerId);
        } catch (Exception e) {
            logger.error("메시지 읽음 처리 중 오류 발생: {}", payload, e);
            return null;
        }
    }
    
    @MessageMapping("/chat.userActive")
    public void userActive(@RequestBody Map<String, String> payload) {
        Long chatRoomId = Long.parseLong(payload.get("chatRoomId"));
        String userId = payload.get("userId");
        chatService.markMessagesAsRead(chatRoomId, userId);
    }
}