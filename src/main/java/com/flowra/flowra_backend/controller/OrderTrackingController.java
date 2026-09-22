package com.flowra.flowra_backend.controller;

import com.flowra.flowra_backend.dto.OrderTrackingDTO;
import com.flowra.flowra_backend.service.OrderTrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order-tracking")
@RequiredArgsConstructor
public class OrderTrackingController {

    private final OrderTrackingService orderTrackingService;

    @PostMapping
    public ResponseEntity<OrderTrackingDTO.Response> addOrUpdateTracking(@Valid @RequestBody OrderTrackingDTO.Request request) {
        return ResponseEntity.ok(orderTrackingService.addOrUpdateTrackingStep(request));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderTrackingDTO.Response>> getTrackingByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderTrackingService.getTrackingByOrderId(orderId));
    }
}
