package com.bookdream.sbb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.bookdream.sbb.trade.chat.ChatService;

import java.security.Principal;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private ChatService chatService;

    @ModelAttribute("totalNewMessagesCount")
    public int getTotalNewMessagesCount(Principal principal) {
        if (principal == null) {
            return 0;
        }
        String userId = principal.getName();
        return chatService.getTotalNewMessagesCount(userId);
    }
}
