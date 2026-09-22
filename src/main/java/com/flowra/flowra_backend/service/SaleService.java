package com.flowra.flowra_backend.service;

import com.flowra.flowra_backend.dto.SaleDTO;
import com.flowra.flowra_backend.entity.Flower;
import com.flowra.flowra_backend.entity.Sale;
import com.flowra.flowra_backend.entity.Stock;
import com.flowra.flowra_backend.exception.BadRequestException;
import com.flowra.flowra_backend.repository.FlowerRepository;
import com.flowra.flowra_backend.repository.SaleRepository;
import com.flowra.flowra_backend.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final FlowerRepository flowerRepository;
    private final StockRepository stockRepository;

    @Transactional(readOnly = true)
    public List<SaleDTO.Response> getAllSales() {
        return saleRepository.findAllByOrderBySaleTimeDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SaleDTO.Response> getSalesByType(String type) {
        return saleRepository.findBySaleTypeOrderBySaleTimeDesc(type).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Records a sale (POS or ONLINE) with pessimistic row-level locking.
     * Prevents negative stock counts and ensures atomicity.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public SaleDTO.Response recordSale(SaleDTO.Request request) {
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new BadRequestException("Quantity must be at least 1");
        }

        // Resolve Flower
        Flower flower = null;
        if (request.getFlowerId() != null) {
            flower = flowerRepository.findById(request.getFlowerId()).orElse(null);
        }
        if (flower == null && request.getFlowerName() != null && !request.getFlowerName().trim().isEmpty()) {
            flower = flowerRepository.findByNameIgnoreCase(request.getFlowerName().trim()).orElse(null);
        }
        if (flower == null) {
            throw new BadRequestException("Flower not found or specified");
        }

        // PESSIMISTIC ROW-LEVEL LOCK ON STOCK ROW
        final Flower finalFlower = flower;
        Stock stock = stockRepository.findByFlowerIdWithLock(finalFlower.getId())
                .orElseThrow(() -> new BadRequestException("No stock record initialized for " + finalFlower.getName()));

        int requestedQty = request.getQuantity();
        if (stock.getCurrentStock() < requestedQty) {
            throw new BadRequestException("Insufficient stock for " + finalFlower.getName() +
                    ": Requested " + requestedQty + ", but only " + stock.getCurrentStock() + " stems available.");
        }

        // Deduct from live stock
        stock.setTotalOutflow(stock.getTotalOutflow() + requestedQty);
        stock.setCurrentStock(stock.getTotalInflow() - stock.getTotalOutflow());
        Stock savedStock = stockRepository.save(stock);

        // Create Sale record
        String receipt = request.getReceiptNumber();
        if (receipt == null || receipt.trim().isEmpty()) {
            receipt = "POS-" + (1000 + (int)(Math.random() * 9000));
        }

        BigDecimal unitPrice = request.getUnitPrice() != null ? request.getUnitPrice() : flower.getPrice();
        BigDecimal discount = request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal total = request.getTotalAmount() != null ? request.getTotalAmount()
                : unitPrice.multiply(BigDecimal.valueOf(requestedQty)).subtract(discount);
        if (total.compareTo(BigDecimal.ZERO) < 0) total = BigDecimal.ZERO;

        Sale sale = Sale.builder()
                .receiptNumber(receipt)
                .flower(flower)
                .quantity(requestedQty)
                .unitPrice(unitPrice)
                .discountAmount(discount)
                .totalAmount(total)
                .paymentMode(request.getPaymentMode() != null ? request.getPaymentMode() : "Cash")
                .customerName(request.getCustomerName() != null ? request.getCustomerName() : "Walk-in Guest")
                .customerPhone(request.getCustomerPhone())
                .attendantName(request.getAttendantName() != null ? request.getAttendantName() : "Counter Staff")
                .saleType(request.getSaleType() != null ? request.getSaleType() : "POS")
                .orderId(request.getOrderId())
                .saleTime(LocalDateTime.now())
                .build();

        Sale savedSale = saleRepository.save(sale);

        return mapToResponse(savedSale, savedStock.getCurrentStock());
    }

    public SaleDTO.Response mapToResponse(Sale s) {
        Integer remaining = stockRepository.findByFlowerId(s.getFlower().getId())
                .map(Stock::getCurrentStock)
                .orElse(0);
        return mapToResponse(s, remaining);
    }

    public SaleDTO.Response mapToResponse(Sale s, Integer remainingStock) {
        return SaleDTO.Response.builder()
                .id(s.getId())
                .receiptNumber(s.getReceiptNumber())
                .flowerId(s.getFlower().getId())
                .flowerName(s.getFlower().getName())
                .categoryName(s.getFlower().getCategory() != null ? s.getFlower().getCategory().getName() : "")
                .quantity(s.getQuantity())
                .unitPrice(s.getUnitPrice())
                .discountAmount(s.getDiscountAmount())
                .totalAmount(s.getTotalAmount())
                .paymentMode(s.getPaymentMode())
                .customerName(s.getCustomerName())
                .customerPhone(s.getCustomerPhone())
                .attendantName(s.getAttendantName())
                .saleType(s.getSaleType())
                .orderId(s.getOrderId())
                .remainingStock(remainingStock)
                .saleTime(s.getSaleTime())
                .build();
    }
}
