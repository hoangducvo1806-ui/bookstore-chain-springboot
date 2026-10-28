package com.bookstore.bookstore.service;

import com.bookstore.bookstore.dto.ChatMessageDTO;
import com.bookstore.bookstore.entity.ChatMessage;
import com.bookstore.bookstore.entity.User;

import java.util.List;

public interface ChatService {
    ChatMessageDTO saveAndFormat(Long senderId, Long receiverId, String content);
    List<ChatMessageDTO> getConversation(Long user1Id, Long user2Id);
    List<ChatMessageDTO> getRecentMessages();
    List<User> getCustomersForVendor(User vendor);
}
