package com.bookstore.bookstore.repository;

import com.bookstore.bookstore.entity.ChatMessage;
import com.bookstore.bookstore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT m FROM ChatMessage m WHERE (m.sender = :u1 AND m.receiver = :u2) OR (m.sender = :u2 AND m.receiver = :u1) ORDER BY m.sentAt ASC")
    List<ChatMessage> findConversation(@Param("u1") User u1, @Param("u2") User u2);

    @Query("SELECT DISTINCT CASE WHEN m.sender = :user THEN m.receiver ELSE m.sender END FROM ChatMessage m WHERE m.sender = :user OR m.receiver = :user")
    List<User> findChatPartners(@Param("user") User user);

    List<ChatMessage> findTop50ByOrderBySentAtDesc();
}
