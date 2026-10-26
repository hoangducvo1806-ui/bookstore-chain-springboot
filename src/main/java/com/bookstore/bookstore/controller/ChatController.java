package com.bookstore.bookstore.controller;

import com.bookstore.bookstore.dto.ChatMessageDTO;
import com.bookstore.bookstore.entity.Shop;
import com.bookstore.bookstore.entity.User;
import com.bookstore.bookstore.repository.ShopRepository;
import com.bookstore.bookstore.repository.UserRepository;
import com.bookstore.bookstore.service.ChatService;
import com.bookstore.bookstore.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/messages")
    public ChatMessageDTO sendMessage(@Payload ChatMessageDTO messageDTO) {
        if (messageDTO.getSenderId() != null && messageDTO.getReceiverId() != null) {
            return chatService.saveAndFormat(
                    messageDTO.getSenderId(),
                    messageDTO.getReceiverId(),
                    messageDTO.getContent()
            );
        }
        return messageDTO;
    }

    @GetMapping("/chat")
    public String chatPage(
            @RequestParam(value = "receiverId", required = false) Long receiverId,
            Model model) {
        User currentUser = securityUtils.getCurrentUser()
                .orElse(null);

        if (currentUser == null) {
            return "redirect:/login";
        }

        List<Shop> shops = shopRepository.findAll();
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("shops", shops);
        model.addAttribute("targetReceiverId", receiverId);

        return "user/chat";
    }

    @GetMapping("/api/chat/history")
    @ResponseBody
    public List<ChatMessageDTO> getHistory(
            @RequestParam Long user1Id,
            @RequestParam Long user2Id) {
        return chatService.getConversation(user1Id, user2Id);
    }
}
