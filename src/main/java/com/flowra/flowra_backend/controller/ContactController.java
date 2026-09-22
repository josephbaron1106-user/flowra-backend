package com.flowra.flowra_backend.controller;

import com.flowra.flowra_backend.entity.ContactMessage;
import com.flowra.flowra_backend.repository.ContactMessageRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactMessageRepository contactMessageRepository;

    @Data
    public static class ContactRequest {
        private String name;
        private String email;
        private String subject;
        private String message;
    }

    @PostMapping
    public ResponseEntity<ContactMessage> createMessage(@RequestBody ContactRequest request) {
        ContactMessage msg = ContactMessage.builder()
                .name(request.getName())
                .email(request.getEmail())
                .subject(request.getSubject() != null && !request.getSubject().isBlank() ? request.getSubject() : "General Inquiry")
                .message(request.getMessage())
                .status("New")
                .build();
        return new ResponseEntity<>(contactMessageRepository.save(msg), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ContactMessage>> getAllMessages() {
        return ResponseEntity.ok(contactMessageRepository.findAll());
    }
}
