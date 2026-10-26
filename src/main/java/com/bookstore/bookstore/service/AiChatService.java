package com.bookstore.bookstore.service;

import com.bookstore.bookstore.dto.AiChatRequest;
import com.bookstore.bookstore.dto.AiChatResponse;

import java.util.List;

public interface AiChatService {

    AiChatResponse chat(AiChatRequest request);

    List<String> getQuickPrompts();
}
