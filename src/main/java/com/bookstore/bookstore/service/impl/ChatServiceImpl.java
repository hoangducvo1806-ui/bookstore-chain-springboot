package com.bookstore.bookstore.service.impl;

import com.bookstore.bookstore.dto.ChatMessageDTO;
import com.bookstore.bookstore.entity.ChatMessage;
import com.bookstore.bookstore.entity.Shop;
import com.bookstore.bookstore.entity.User;
import com.bookstore.bookstore.enums.RoleName;
import com.bookstore.bookstore.repository.ChatMessageRepository;
import com.bookstore.bookstore.repository.OrderRepository;
import com.bookstore.bookstore.repository.ShopRepository;
import com.bookstore.bookstore.repository.UserRepository;
import com.bookstore.bookstore.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final OrderRepository orderRepository;

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

    @Override
    public List<User> getCustomersForVendor(User vendor) {
        Map<Long, User> customerMap = new LinkedHashMap<>();

        // 1. Những khách hàng đã từng trò chuyện với Vendor
        try {
            List<User> partners = chatMessageRepository.findChatPartners(vendor);
            if (partners != null) {
                for (User p : partners) {
                    if (p != null && !p.getId().equals(vendor.getId())) {
                        customerMap.put(p.getId(), p);
                    }
                }
            }
        } catch (Exception ignored) {}

        // 2. Những khách hàng đã từng đặt đơn tại cửa hàng của Vendor
        try {
            Shop shop = shopRepository.findByOwner(vendor).orElse(null);
            if (shop != null) {
                List<User> orderCustomers = orderRepository.findDistinctCustomersByShop(shop);
                if (orderCustomers != null) {
                    for (User c : orderCustomers) {
                        if (c != null && !c.getId().equals(vendor.getId())) {
                            customerMap.putIfAbsent(c.getId(), c);
                        }
                    }
                }
            }
        } catch (Exception ignored) {}

        // 3. Bổ sung các tài khoản khách hàng thông thường để người bán luôn có thể chủ động liên hệ
        try {
            List<User> normalUsers = userRepository.findByRoles_Name(RoleName.USER);
            if (normalUsers != null) {
                for (User u : normalUsers) {
                    if (u != null && !u.getId().equals(vendor.getId())) {
                        customerMap.putIfAbsent(u.getId(), u);
                    }
                }
            }
        } catch (Exception ignored) {}

        return new ArrayList<>(customerMap.values());
    }
}
