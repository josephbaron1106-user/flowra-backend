package com.flowra.flowra_backend.service;

import com.flowra.flowra_backend.dto.OrderTrackingDTO;
import com.flowra.flowra_backend.entity.Order;
import com.flowra.flowra_backend.entity.OrderTracking;
import com.flowra.flowra_backend.exception.ResourceNotFoundException;
import com.flowra.flowra_backend.repository.OrderRepository;
import com.flowra.flowra_backend.repository.OrderTrackingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderTrackingService {

    private final OrderTrackingRepository orderTrackingRepository;
    private final OrderRepository orderRepository;

    public List<OrderTrackingDTO.Response> getTrackingByOrderId(Long orderId) {
        return orderTrackingRepository.findByOrderIdOrderByStepNumberAsc(orderId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderTrackingDTO.Response addOrUpdateTrackingStep(OrderTrackingDTO.Request request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + request.getOrderId()));

        int stepNumber = request.getStepNumber() != null ? request.getStepNumber() : 1;

        // Check if step exists
        OrderTracking tracking = orderTrackingRepository.findByOrderIdOrderByStepNumberAsc(order.getId()).stream()
                .filter(t -> t.getStepNumber().equals(stepNumber))
                .findFirst()
                .orElseGet(() -> OrderTracking.builder()
                        .order(order)
                        .stepNumber(stepNumber)
                        .build());

        if (request.getStatusTitle() != null) tracking.setStatusTitle(request.getStatusTitle());
        if (request.getStatusDescription() != null) tracking.setStatusDescription(request.getStatusDescription());
        if (request.getDriverName() != null) tracking.setDriverName(request.getDriverName());
        if (request.getDriverPhone() != null) tracking.setDriverPhone(request.getDriverPhone());
        if (request.getEstimatedDeliveryTime() != null) tracking.setEstimatedDeliveryTime(request.getEstimatedDeliveryTime());

        if (Boolean.TRUE.equals(request.getIsCompleted())) {
            tracking.setIsCompleted(true);
            if (tracking.getCompletedAt() == null) tracking.setCompletedAt(LocalDateTime.now());
        } else if (Boolean.FALSE.equals(request.getIsCompleted())) {
            tracking.setIsCompleted(false);
            tracking.setCompletedAt(null);
        }

        OrderTracking saved = orderTrackingRepository.save(tracking);
        return mapToResponse(saved);
    }

    public OrderTrackingDTO.Response mapToResponse(OrderTracking tracking) {
        return OrderTrackingDTO.Response.builder()
                .id(tracking.getId())
                .orderId(tracking.getOrder().getId())
                .stepNumber(tracking.getStepNumber())
                .statusTitle(tracking.getStatusTitle())
                .statusDescription(tracking.getStatusDescription())
                .isCompleted(tracking.getIsCompleted())
                .completedAt(tracking.getCompletedAt())
                .driverName(tracking.getDriverName())
                .driverPhone(tracking.getDriverPhone())
                .estimatedDeliveryTime(tracking.getEstimatedDeliveryTime())
                .createdAt(tracking.getCreatedAt())
                .build();
    }
}
