package com.flowra.flowra_backend.controller;

import com.flowra.flowra_backend.entity.NewsletterSubscriber;
import com.flowra.flowra_backend.repository.NewsletterSubscriberRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/newsletter")
@RequiredArgsConstructor
public class NewsletterController {

    private final NewsletterSubscriberRepository subscriberRepository;

    @Data
    public static class SubscribeRequest {
        private String email;
    }

    @PostMapping
    public ResponseEntity<?> subscribe(@RequestBody SubscribeRequest req) {
        if (req.getEmail() == null || req.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        }
        String cleanEmail = req.getEmail().trim().toLowerCase();
        NewsletterSubscriber subscriber = subscriberRepository.findByEmail(cleanEmail)
                .map(existing -> {
                    existing.setIsActive(true);
                    return existing;
                })
                .orElseGet(() -> NewsletterSubscriber.builder()
                        .email(cleanEmail)
                        .isActive(true)
                        .build());

        NewsletterSubscriber saved = subscriberRepository.save(subscriber);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<NewsletterSubscriber>> getAllSubscribers() {
        return ResponseEntity.ok(subscriberRepository.findAll());
    }
}
