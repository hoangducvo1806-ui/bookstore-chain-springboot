package com.bookstore.bookstore.controller;

import com.bookstore.bookstore.dto.AiChatRequest;
import com.bookstore.bookstore.dto.AiChatResponse;
import com.bookstore.bookstore.service.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(@RequestBody(required = false) AiChatRequest request) {
        if (request == null) {
            request = new AiChatRequest();
        }
        AiChatResponse response = aiChatService.chat(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/quick-prompts")
    public ResponseEntity<List<String>> getQuickPrompts() {
        return ResponseEntity.ok(aiChatService.getQuickPrompts());
    }
}
