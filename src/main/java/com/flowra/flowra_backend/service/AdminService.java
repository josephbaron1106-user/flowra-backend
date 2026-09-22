package com.flowra.flowra_backend.service;

import com.flowra.flowra_backend.dto.AdminDashboardDTO;
import com.flowra.flowra_backend.dto.OrderDTO;
import com.flowra.flowra_backend.entity.AdminActivity;
import com.flowra.flowra_backend.entity.Order;
import com.flowra.flowra_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;
    private final AdminActivityRepository adminActivityRepository;
    private final OrderService orderService;

    public AdminDashboardDTO.DashboardResponse getDashboardStats() {
        List<Order> orders = orderRepository.findAll();

        BigDecimal totalRevenue = orders.stream()
                .filter(o -> !"CANCELLED".equalsIgnoreCase(o.getStatus()))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalOrders = orders.size();
        long pendingOrders = orders.stream()
                .filter(o -> Arrays.asList("PLACED", "CONFIRMED", "PROCESSING", "OUT_FOR_DELIVERY").contains(o.getStatus().toUpperCase()))
                .count();
        long deliveredOrders = orders.stream()
                .filter(o -> "DELIVERED".equalsIgnoreCase(o.getStatus()))
                .count();

        long totalCustomers = userRepository.count();
        long totalProducts = productRepository.count();
        long lowStockCount = inventoryRepository.findLowStockInventory().size();

        List<AdminDashboardDTO.ActivityResponse> recentActivities = adminActivityRepository.findTop10ByOrderByCreatedAtDesc().stream()
                .map(this::mapActivityToResponse)
                .collect(Collectors.toList());

        return AdminDashboardDTO.DashboardResponse.builder()
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .deliveredOrders(deliveredOrders)
                .totalCustomers(totalCustomers)
                .totalProducts(totalProducts)
                .lowStockCount(lowStockCount)
                .recentActivities(recentActivities)
                .build();
    }

    public List<OrderDTO.Response> getCurrentOrders() {
        List<String> activeStatuses = Arrays.asList("PLACED", "CONFIRMED", "PROCESSING", "OUT_FOR_DELIVERY", "Out for Delivery");
        return orderRepository.findByStatusInOrderByPlacedAtDesc(activeStatuses).stream()
                .map(orderService::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<OrderDTO.Response> getOrderHistory() {
        List<String> finishedStatuses = Arrays.asList("DELIVERED", "CANCELLED", "Delivered", "Cancelled");
        return orderRepository.findByStatusInOrderByPlacedAtDesc(finishedStatuses).stream()
                .map(orderService::mapToResponse)
                .collect(Collectors.toList());
    }

    private AdminDashboardDTO.ActivityResponse mapActivityToResponse(AdminActivity activity) {
        return AdminDashboardDTO.ActivityResponse.builder()
                .id(activity.getId())
                .type(activity.getActivityType())
                .title(activity.getTitle())
                .detail(activity.getDetail())
                .amount(activity.getAmount())
                .icon(activity.getIcon())
                .badgeClass(activity.getBadgeClass())
                .time(activity.getCreatedAt())
                .build();
    }
}
