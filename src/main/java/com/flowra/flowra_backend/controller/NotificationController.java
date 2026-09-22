package com.flowra.flowra_backend.controller;

import com.flowra.flowra_backend.entity.Notification;
import com.flowra.flowra_backend.repository.NotificationRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @Data
    public static class NotificationRequest {
        private String notificationType;
        private String title;
        private String message;
    }

    @PostMapping
    public ResponseEntity<Notification> createNotification(@RequestBody NotificationRequest req) {
        Notification n = Notification.builder()
                .notificationType(req.getNotificationType() != null ? req.getNotificationType() : "system")
                .title(req.getTitle())
                .message(req.getMessage())
                .isRead(false)
                .build();
        return new ResponseEntity<>(notificationRepository.save(n), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        return ResponseEntity.ok(notificationRepository.findAll());
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        Notification n = notificationRepository.findById(id).orElse(null);
        if (n == null) return ResponseEntity.notFound().build();
        n.setIsRead(true);
        return ResponseEntity.ok(notificationRepository.save(n));
    }

    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead() {
        List<Notification> list = notificationRepository.findAll();
        list.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(list);
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
    }
}
