package com.bookstore.bookstore.controller;

import com.bookstore.bookstore.entity.Book;
import com.bookstore.bookstore.entity.ChatMessage;
import com.bookstore.bookstore.entity.User;
import com.bookstore.bookstore.repository.BookRepository;
import com.bookstore.bookstore.repository.ChatMessageRepository;
import com.bookstore.bookstore.repository.UserRepository;
import com.bookstore.bookstore.service.NotificationService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final NotificationService notificationService;

    @Data
    public static class ChatPayload {
        private Long senderId;
        private Long receiverId;
        private Long bookId;
        private String content;
    }

    @MessageMapping("/chat.send")
    public void handleChatMessage(@Payload ChatPayload payload) {
        if (payload == null || payload.getSenderId() == null || payload.getReceiverId() == null || payload.getContent() == null) {
            return;
        }

        try {
            User sender = userRepository.findById(payload.getSenderId()).orElse(null);
            User receiver = userRepository.findById(payload.getReceiverId()).orElse(null);
            if (sender == null || receiver == null) return;

            Book book = payload.getBookId() != null ? bookRepository.findById(payload.getBookId()).orElse(null) : null;

            ChatMessage message = ChatMessage.builder()
                    .sender(sender)
                    .receiver(receiver)
                    .book(book)
                    .content(payload.getContent().trim())
                    .isRead(false)
                    .sentAt(LocalDateTime.now())
                    .build();

            message = chatMessageRepository.save(message);
            notificationService.sendChatMessage(message);
            log.info("WebSocket Chat: Đã chuyển tin nhắn từ {} sang {}", sender.getEmail(), receiver.getEmail());
        } catch (Exception e) {
            log.error("Lỗi gửi tin nhắn qua WebSocket: {}", e.getMessage(), e);
        }
    }

    @GetMapping("/api/chat/history")
    @ResponseBody
    public ResponseEntity<?> getChatHistory(@RequestParam Long user1, @RequestParam Long user2) {
        User u1 = userRepository.findById(user1).orElse(null);
        User u2 = userRepository.findById(user2).orElse(null);
        if (u1 == null || u2 == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Người dùng không tồn tại"));
        }

        List<ChatMessage> messages = chatMessageRepository.findConversation(u1, u2);
        return ResponseEntity.ok(messages);
    }
}
