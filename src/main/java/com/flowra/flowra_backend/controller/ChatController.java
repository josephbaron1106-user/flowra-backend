package com.flowra.flowra_backend.controller;

import com.flowra.flowra_backend.entity.ChatConversation;
import com.flowra.flowra_backend.entity.ChatMessage;
import com.flowra.flowra_backend.repository.ChatConversationRepository;
import com.flowra.flowra_backend.repository.ChatMessageRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;

    @Data
    public static class NewMessageRequest {
        private Long conversationId;
        private String customerName;
        private String senderType; // 'customer', 'admin'
        private String messageText;
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<ChatConversation>> getConversations() {
        return ResponseEntity.ok(conversationRepository.findAllByOrderByUpdatedAtDesc());
    }

    @GetMapping("/conversations/{id}/messages")
    public ResponseEntity<List<ChatMessage>> getMessages(@PathVariable Long id) {
        return ResponseEntity.ok(messageRepository.findByConversationIdOrderBySentAtAsc(id));
    }

    @PostMapping("/messages")
    public ResponseEntity<ChatMessage> sendMessage(@RequestBody NewMessageRequest req) {
        ChatConversation conv;
        if (req.getConversationId() != null) {
            conv = conversationRepository.findById(req.getConversationId()).orElse(null);
        } else {
            conv = conversationRepository.save(ChatConversation.builder()
                    .customerName(req.getCustomerName() != null ? req.getCustomerName() : "Customer")
                    .isOnline(true)
                    .unreadCount(0)
                    .lastMessage(req.getMessageText())
                    .build());
        }

        if (conv != null) {
            conv.setLastMessage(req.getMessageText());
            conversationRepository.save(conv);
        }

        ChatMessage msg = ChatMessage.builder()
                .conversation(conv)
                .senderType(req.getSenderType() != null ? req.getSenderType() : "admin")
                .messageText(req.getMessageText())
                .build();

        return new ResponseEntity<>(messageRepository.save(msg), HttpStatus.CREATED);
    }
}
