package com.flowra.flowra_backend.controller;

import com.flowra.flowra_backend.entity.DeliveryAssignment;
import com.flowra.flowra_backend.repository.DeliveryAssignmentRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryAssignmentController {

    private final DeliveryAssignmentRepository deliveryRepository;

    @Data
    public static class DeliveryRequest {
        private String deliveryCode;
        private String orderNumber;
        private String customerName;
        private String deliveryAddress;
        private String deliveryPartner;
        private String driverName;
        private String driverPhone;
        private String deliverySlot;
        private String status;
        private String temperatureLog;
    }

    @PostMapping
    public ResponseEntity<DeliveryAssignment> createDelivery(@RequestBody DeliveryRequest req) {
        String code = req.getDeliveryCode();
        if (code == null || code.isBlank()) {
            code = "DEL-" + (300 + (int)(Math.random() * 700));
        }

        DeliveryAssignment deliv = DeliveryAssignment.builder()
                .deliveryCode(code)
                .orderNumber(req.getOrderNumber() != null ? req.getOrderNumber() : "FLW-" + System.currentTimeMillis() % 100000)
                .customerName(req.getCustomerName())
                .deliveryAddress(req.getDeliveryAddress())
                .deliveryPartner(req.getDeliveryPartner() != null ? req.getDeliveryPartner() : "Flowra Express Fleet")
                .driverName(req.getDriverName() != null ? req.getDriverName() : "Assigned Driver")
                .driverPhone(req.getDriverPhone())
                .deliverySlot(req.getDeliverySlot() != null ? req.getDeliverySlot() : "Morning Slot")
                .status(req.getStatus() != null ? req.getStatus() : "Assigned")
                .temperatureLog(req.getTemperatureLog() != null ? req.getTemperatureLog() : "15°C")
                .build();

        return new ResponseEntity<>(deliveryRepository.save(deliv), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<DeliveryAssignment>> getAllDeliveries() {
        return ResponseEntity.ok(deliveryRepository.findAll());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        DeliveryAssignment d = deliveryRepository.findById(id).orElse(null);
        if (d == null) return ResponseEntity.notFound().build();
        String newStatus = body.get("status");
        if (newStatus != null) {
            d.setStatus(newStatus);
            if ("Delivered".equalsIgnoreCase(newStatus)) {
                d.setDeliveredAt(LocalDateTime.now());
            }
        }
        return ResponseEntity.ok(deliveryRepository.save(d));
    }
}
