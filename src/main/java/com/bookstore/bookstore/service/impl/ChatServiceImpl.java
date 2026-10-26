package com.bookstore.bookstore.service.impl;

import com.bookstore.bookstore.dto.ChatMessageDTO;
import com.bookstore.bookstore.entity.ChatMessage;
import com.bookstore.bookstore.entity.User;
import com.bookstore.bookstore.repository.ChatMessageRepository;
import com.bookstore.bookstore.repository.UserRepository;
import com.bookstore.bookstore.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    @Override
    @Transactional
    public ChatMessageDTO saveAndFormat(Long senderId, Long receiverId, String content) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Người gửi không tồn tại"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Người nhận không tồn tại"));

        ChatMessage message = ChatMessage.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .isRead(false)
                .build();

        ChatMessage saved = chatMessageRepository.save(message);

        return ChatMessageDTO.builder()
                .id(saved.getId())
                .senderId(sender.getId())
                .senderName(sender.getFullName())
                .receiverId(receiver.getId())
                .receiverName(receiver.getFullName())
                .content(saved.getContent())
                .sentAt(saved.getSentAt() != null ? saved.getSentAt().format(FORMATTER) : "Vừa xong")
                .build();
    }

    @Override
    public List<ChatMessageDTO> getConversation(Long user1Id, Long user2Id) {
        User u1 = userRepository.findById(user1Id).orElse(null);
        User u2 = userRepository.findById(user2Id).orElse(null);
        if (u1 == null || u2 == null) {
            return Collections.emptyList();
        }

        return chatMessageRepository.findConversation(u1, u2).stream()
                .map(m -> ChatMessageDTO.builder()
                        .id(m.getId())
                        .senderId(m.getSender().getId())
                        .senderName(m.getSender().getFullName())
                        .receiverId(m.getReceiver().getId())
                        .receiverName(m.getReceiver().getFullName())
                        .content(m.getContent())
                        .sentAt(m.getSentAt() != null ? m.getSentAt().format(FORMATTER) : "")
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<ChatMessageDTO> getRecentMessages() {
        return chatMessageRepository.findTop50ByOrderBySentAtDesc().stream()
                .map(m -> ChatMessageDTO.builder()
                        .id(m.getId())
                        .senderId(m.getSender().getId())
                        .senderName(m.getSender().getFullName())
                        .receiverId(m.getReceiver().getId())
                        .receiverName(m.getReceiver().getFullName())
                        .content(m.getContent())
                        .sentAt(m.getSentAt() != null ? m.getSentAt().format(FORMATTER) : "")
                        .build())
                .collect(Collectors.toList());
    }
}
