package com.flowra.flowra_backend.service;

import com.flowra.flowra_backend.dto.OrderDTO;
import com.flowra.flowra_backend.dto.OrderTrackingDTO;
import com.flowra.flowra_backend.dto.PaymentDTO;
import com.flowra.flowra_backend.entity.*;
import com.flowra.flowra_backend.exception.BadRequestException;
import com.flowra.flowra_backend.exception.ResourceNotFoundException;
import com.flowra.flowra_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final PaymentRepository paymentRepository;
    private final OrderTrackingRepository orderTrackingRepository;
    private final AdminActivityRepository adminActivityRepository;
    private final FlowerRepository flowerRepository;
    private final StockRepository stockRepository;
    private final SaleRepository saleRepository;

    @Transactional
    public OrderDTO.Response createOrder(OrderDTO.CreateRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Cannot create an order without items");
        }

        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId()).orElse(null);
        }

        String orderNumber = request.getOrderNumber();
        if (orderNumber == null || orderNumber.trim().isEmpty()) {
            orderNumber = "FLW-" + (System.currentTimeMillis() % 1000000);
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .user(user)
                .status("PROCESSING")
                .recipientName(request.getRecipientName())
                .recipientPhone(request.getRecipientPhone())
                .deliveryAddress(request.getDeliveryAddress())
                .deliveryDate(request.getDeliveryDate() != null ? request.getDeliveryDate() : LocalDate.now().plusDays(1))
                .deliverySlot(request.getDeliverySlot() != null ? request.getDeliverySlot() : "Morning (8:00 AM - 11:00 AM)")
                .giftMessage(request.getGiftMessage())
                .occasion(request.getOccasion() != null ? request.getOccasion() : "Birthday")
                .promoCode(request.getPromoCode())
                .discountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO)
                .deliveryFee(request.getDeliveryFee() != null ? request.getDeliveryFee() : BigDecimal.ZERO)
                .subtotal(BigDecimal.ZERO) // temporary
                .totalAmount(BigDecimal.ZERO) // temporary
                .build();

        Order savedOrder = orderRepository.save(order);

        for (OrderDTO.CreateItemRequest itemReq : request.getItems()) {
            Product product = null;
            String name = itemReq.getProductName();
            BigDecimal price = itemReq.getUnitPrice();
            String image = itemReq.getProductImage();

            if (itemReq.getProductId() != null) {
                product = productRepository.findById(itemReq.getProductId()).orElse(null);
                if (product != null) {
                    if (name == null) name = product.getName();
                    if (price == null) price = product.getPrice();

                    // Adjust legacy inventory if present
                    inventoryRepository.findByProductId(product.getId()).ifPresent(inv -> {
                        int qty = itemReq.getQuantity() != null ? itemReq.getQuantity() : 1;
                        inv.setCurrentStock(Math.max(0, inv.getCurrentStock() - qty));
                        inventoryRepository.save(inv);
                    });
                }
            }

            if (price == null) price = BigDecimal.ZERO;
            int quantity = itemReq.getQuantity() != null && itemReq.getQuantity() > 0 ? itemReq.getQuantity() : 1;

            // Atomic Stock Deduction for Flowers with Pessimistic Row Locking
            Flower flower = null;
            if (itemReq.getProductId() != null) {
                flower = flowerRepository.findById(itemReq.getProductId()).orElse(null);
            }
            if (flower == null && name != null && !name.trim().isEmpty()) {
                flower = flowerRepository.findByNameIgnoreCase(name.trim()).orElse(null);
            }
            if (flower != null) {
                Stock flowerStock = stockRepository.findByFlowerIdWithLock(flower.getId()).orElse(null);
                if (flowerStock != null) {
                    if (flowerStock.getCurrentStock() < quantity) {
                        throw new BadRequestException("Insufficient stock for " + flower.getName() +
                                ": Only " + flowerStock.getCurrentStock() + " stems available, but " + quantity + " requested.");
                    }
                    flowerStock.setTotalOutflow(flowerStock.getTotalOutflow() + quantity);
                    flowerStock.setCurrentStock(flowerStock.getTotalInflow() - flowerStock.getTotalOutflow());
                    stockRepository.save(flowerStock);

                    // Record ONLINE Sale in sales table
                    Sale onlineSale = Sale.builder()
                            .receiptNumber("ONL-" + savedOrder.getOrderNumber() + "-" + (int)(Math.random() * 900 + 100))
                            .flower(flower)
                            .quantity(quantity)
                            .unitPrice(price)
                            .discountAmount(BigDecimal.ZERO)
                            .totalAmount(price.multiply(BigDecimal.valueOf(quantity)))
                            .paymentMode("Online Checkout")
                            .customerName(savedOrder.getRecipientName() != null ? savedOrder.getRecipientName() : "Online Customer")
                            .customerPhone(savedOrder.getRecipientPhone())
                            .attendantName("Storefront System")
                            .saleType("ONLINE")
                            .orderId(savedOrder.getId())
                            .saleTime(LocalDateTime.now())
                            .build();
                    saleRepository.save(onlineSale);
                }
            }
            BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(quantity));
            subtotal = subtotal.add(lineTotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(savedOrder)
                    .product(product)
                    .productName(name != null ? name : "Artisan Bouquet")
                    .unitPrice(price)
                    .quantity(quantity)
                    .totalPrice(lineTotal)
                    .productImage(image)
                    .build();

            orderItems.add(orderItemRepository.save(orderItem));
        }

        BigDecimal discount = savedOrder.getDiscountAmount();
        BigDecimal delivery = savedOrder.getDeliveryFee();
        BigDecimal total = subtotal.subtract(discount).add(delivery);
        if (total.compareTo(BigDecimal.ZERO) < 0) total = BigDecimal.ZERO;

        savedOrder.setSubtotal(subtotal);
        savedOrder.setTotalAmount(total);
        savedOrder.setItems(orderItems);
        Order updatedOrder = orderRepository.save(savedOrder);

        // Create 4 initial tracking steps
        createInitialTrackingSteps(updatedOrder);

        // Create payment record
        if (request.getPaymentMethod() != null) {
            boolean isOnline = "razorpay".equalsIgnoreCase(request.getPaymentMethod());
            Payment payment = Payment.builder()
                    .order(updatedOrder)
                    .paymentMethod(request.getPaymentMethod().toLowerCase())
                    .paymentStatus(isOnline ? "Paid" : "Pending COD")
                    .amount(total)
                    .currency("INR")
                    .razorpayOrderId(request.getRazorpayOrderId())
                    .razorpayPaymentId(request.getPaymentId())
                    .paidAt(isOnline ? LocalDateTime.now() : null)
                    .build();
            paymentRepository.save(payment);
        }

        // Record admin activity
        AdminActivity activity = AdminActivity.builder()
                .admin(user)
                .activityType("online")
                .title("Online Order #" + orderNumber)
                .detail(request.getRecipientName() + " • ₹" + total)
                .amount(total)
                .icon("🌐")
                .badgeClass("badge-green")
                .build();
        adminActivityRepository.save(activity);

        return mapToResponse(updatedOrder);
    }

    private void createInitialTrackingSteps(Order order) {
        String[] titles = {
            "Order Confirmed",
            "Arranging Blooms",
            "Out for Delivery",
            "Delivered"
        };
        String[] descriptions = {
            "Received & verified by florist desk",
            "Greenhouse florist styling",
            "Driver on route with temperature controlled transit",
            "Handed over at doorstep"
        };

        for (int i = 1; i <= 4; i++) {
            OrderTracking tracking = OrderTracking.builder()
                    .order(order)
                    .stepNumber(i)
                    .statusTitle(titles[i - 1])
                    .statusDescription(descriptions[i - 1])
                    .isCompleted(i == 1) // First step completed
                    .completedAt(i == 1 ? LocalDateTime.now() : null)
                    .build();
            orderTrackingRepository.save(tracking);
        }
    }

    public List<OrderDTO.Response> getAllOrders() {
        return orderRepository.findAllByOrderByPlacedAtDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<OrderDTO.Response> getOrdersByUser(Long userId) {
        return orderRepository.findByUserIdOrderByPlacedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public OrderDTO.Response getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return mapToResponse(order);
    }

    public OrderDTO.Response getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with number: " + orderNumber));
        return mapToResponse(order);
    }

    @Transactional
    public OrderDTO.Response updateOrderStatus(Long id, OrderDTO.StatusUpdateRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        String newStatus = request.getStatus().toUpperCase();
        order.setStatus(newStatus);
        if (request.getCancellationReason() != null) {
            order.setCancellationReason(request.getCancellationReason());
        }
        if (request.getCancelledBy() != null) {
            order.setCancelledBy(request.getCancelledBy());
        }
        Order updated = orderRepository.save(order);

        // Update tracking steps to match status
        updateTrackingForStatus(updated, newStatus);

        // Automated Payment Status for Cash on Delivery (COD):
        // If order status is marked DELIVERED and payment method is COD, update payment status to Paid atomically
        if ("DELIVERED".equals(newStatus)) {
            List<Payment> payments = paymentRepository.findByOrderId(order.getId());
            for (Payment payment : payments) {
                String method = payment.getPaymentMethod() != null ? payment.getPaymentMethod().toLowerCase() : "";
                if ((method.contains("cod") || method.contains("cash")) && !"Paid".equalsIgnoreCase(payment.getPaymentStatus())) {
                    payment.setPaymentStatus("Paid");
                    if (payment.getPaidAt() == null) {
                        payment.setPaidAt(LocalDateTime.now());
                    }
                    paymentRepository.save(payment);
                }
            }
        }

        return mapToResponse(updated);
    }

    private void updateTrackingForStatus(Order order, String status) {
        List<OrderTracking> steps = orderTrackingRepository.findByOrderIdOrderByStepNumberAsc(order.getId());
        int targetStep = switch (status) {
            case "PLACED", "CONFIRMED" -> 1;
            case "PROCESSING", "ARRANGING BLOOMS", "CRAFTING" -> 2;
            case "OUT_FOR_DELIVERY", "OUT FOR DELIVERY" -> 3;
            case "DELIVERED" -> 4;
            default -> 1;
        };

        for (OrderTracking step : steps) {
            if (step.getStepNumber() <= targetStep) {
                step.setIsCompleted(true);
                if (step.getCompletedAt() == null) {
                    step.setCompletedAt(LocalDateTime.now());
                }
            } else {
                step.setIsCompleted(false);
                step.setCompletedAt(null);
            }
            orderTrackingRepository.save(step);
        }
    }

    public OrderDTO.Response mapToResponse(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        List<OrderDTO.ItemResponse> itemResponses = items.stream().map(item ->
                OrderDTO.ItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                        .productName(item.getProductName())
                        .unitPrice(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .totalPrice(item.getTotalPrice())
                        .productImage(item.getProductImage())
                        .build()
        ).collect(Collectors.toList());

        List<OrderTrackingDTO.Response> trackingResponses = orderTrackingRepository.findByOrderIdOrderByStepNumberAsc(order.getId()).stream()
                .map(t -> OrderTrackingDTO.Response.builder()
                        .id(t.getId())
                        .orderId(order.getId())
                        .stepNumber(t.getStepNumber())
                        .statusTitle(t.getStatusTitle())
                        .statusDescription(t.getStatusDescription())
                        .isCompleted(t.getIsCompleted())
                        .completedAt(t.getCompletedAt())
                        .driverName(t.getDriverName())
                        .driverPhone(t.getDriverPhone())
                        .estimatedDeliveryTime(t.getEstimatedDeliveryTime())
                        .createdAt(t.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        List<PaymentDTO.Response> paymentResponses = paymentRepository.findByOrderId(order.getId()).stream()
                .map(p -> PaymentDTO.Response.builder()
                        .id(p.getId())
                        .orderId(order.getId())
                        .paymentMethod(p.getPaymentMethod())
                        .paymentStatus(p.getPaymentStatus())
                        .amount(p.getAmount())
                        .currency(p.getCurrency())
                        .razorpayOrderId(p.getRazorpayOrderId())
                        .razorpayPaymentId(p.getRazorpayPaymentId())
                        .paidAt(p.getPaidAt())
                        .createdAt(p.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return OrderDTO.Response.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .status(order.getStatus())
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .deliveryFee(order.getDeliveryFee())
                .totalAmount(order.getTotalAmount())
                .promoCode(order.getPromoCode())
                .deliveryDate(order.getDeliveryDate())
                .deliverySlot(order.getDeliverySlot())
                .recipientName(order.getRecipientName())
                .recipientPhone(order.getRecipientPhone())
                .deliveryAddress(order.getDeliveryAddress())
                .giftMessage(order.getGiftMessage())
                .occasion(order.getOccasion())
                .cancellationReason(order.getCancellationReason())
                .cancelledBy(order.getCancelledBy())
                .items(itemResponses)
                .tracking(trackingResponses)
                .payments(paymentResponses)
                .placedAt(order.getPlacedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
