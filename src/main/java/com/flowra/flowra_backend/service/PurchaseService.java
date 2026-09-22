package com.flowra.flowra_backend.service;

import com.flowra.flowra_backend.dto.PurchaseDTO;
import com.flowra.flowra_backend.entity.Flower;
import com.flowra.flowra_backend.entity.Purchase;
import com.flowra.flowra_backend.entity.Stock;
import com.flowra.flowra_backend.entity.Supplier;
import com.flowra.flowra_backend.exception.BadRequestException;
import com.flowra.flowra_backend.exception.ResourceNotFoundException;
import com.flowra.flowra_backend.repository.FlowerRepository;
import com.flowra.flowra_backend.repository.PurchaseRepository;
import com.flowra.flowra_backend.repository.StockRepository;
import com.flowra.flowra_backend.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final FlowerRepository flowerRepository;
    private final SupplierRepository supplierRepository;
    private final StockRepository stockRepository;
    private final SupplierService supplierService;

    @Transactional(readOnly = true)
    public List<PurchaseDTO.Response> getAllPurchases() {
        return purchaseRepository.findAllByOrderByPurchaseDateDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PurchaseDTO.Response recordPurchase(PurchaseDTO.Request request) {
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new BadRequestException("Quantity must be greater than zero");
        }
        if (request.getUnitCost() == null || request.getUnitCost().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Unit cost must be a non-negative value");
        }

        // Flower resolution
        Flower flower = null;
        if (request.getFlowerId() != null) {
            flower = flowerRepository.findById(request.getFlowerId()).orElse(null);
        }
        if (flower == null && request.getFlowerName() != null && !request.getFlowerName().trim().isEmpty()) {
            flower = flowerRepository.findByNameIgnoreCase(request.getFlowerName().trim()).orElse(null);
        }
        if (flower == null) {
            throw new BadRequestException("Flower must be specified and registered in catalog");
        }

        // Supplier resolution
        Supplier supplier = null;
        if (request.getSupplierId() != null) {
            supplier = supplierRepository.findById(request.getSupplierId()).orElse(null);
        }
        if (supplier == null && request.getSupplierName() != null && !request.getSupplierName().trim().isEmpty()) {
            supplier = supplierService.findOrCreateSupplier(request.getSupplierName().trim());
        }
        if (supplier == null) {
            supplier = supplierService.findOrCreateSupplier("Direct Mandi Growers");
        }

        // Purchase code
        String code = request.getPurchaseCode();
        if (code == null || code.trim().isEmpty()) {
            code = "WS-" + LocalDate.now().getYear() + "-" + (100 + (int)(Math.random() * 900));
        }

        LocalDate pDate = LocalDate.now();
        if (request.getPurchaseDate() != null && !request.getPurchaseDate().trim().isEmpty()) {
            try { pDate = LocalDate.parse(request.getPurchaseDate().trim()); } catch (Exception ignored) {}
        }

        BigDecimal unitCost = request.getUnitCost();
        BigDecimal totalCost = unitCost.multiply(BigDecimal.valueOf(request.getQuantity()));
        BigDecimal retail = request.getRetailPrice() != null && request.getRetailPrice().compareTo(BigDecimal.ZERO) > 0
                ? request.getRetailPrice() : flower.getPrice();

        Purchase purchase = Purchase.builder()
                .purchaseCode(code)
                .flower(flower)
                .supplier(supplier)
                .quantity(request.getQuantity())
                .unitCost(unitCost)
                .totalCost(totalCost)
                .retailPrice(retail)
                .grade(request.getGrade() != null ? request.getGrade() : "Grade A")
                .notes(request.getNotes() != null ? request.getNotes() : "Fresh wholesale stock received.")
                .purchaseDate(pDate)
                .build();

        Purchase savedPurchase = purchaseRepository.save(purchase);

        // Atomically lock and increment stock
        final Flower finalFlower = flower;
        Stock stock = stockRepository.findByFlowerIdWithLock(finalFlower.getId())
                .orElseGet(() -> Stock.builder()
                        .flower(finalFlower)
                        .totalInflow(0)
                        .totalOutflow(0)
                        .currentStock(0)
                        .minThreshold(10)
                        .build());

        stock.setTotalInflow(stock.getTotalInflow() + request.getQuantity());
        stock.setCurrentStock(stock.getTotalInflow() - stock.getTotalOutflow());
        stock.setLastRestockedAt(LocalDateTime.now());
        Stock updatedStock = stockRepository.save(stock);

        return mapToResponse(savedPurchase, updatedStock.getCurrentStock());
    }

    public PurchaseDTO.Response mapToResponse(Purchase p) {
        Integer current = stockRepository.findByFlowerId(p.getFlower().getId())
                .map(Stock::getCurrentStock)
                .orElse(0);
        return mapToResponse(p, current);
    }

    public PurchaseDTO.Response mapToResponse(Purchase p, Integer resultingStock) {
        return PurchaseDTO.Response.builder()
                .id(p.getId())
                .purchaseCode(p.getPurchaseCode())
                .flowerId(p.getFlower().getId())
                .flowerName(p.getFlower().getName())
                .flowerSku(p.getFlower().getSku())
                .categoryName(p.getFlower().getCategory() != null ? p.getFlower().getCategory().getName() : "")
                .supplierId(p.getSupplier().getId())
                .supplierName(p.getSupplier().getName())
                .quantity(p.getQuantity())
                .unitCost(p.getUnitCost())
                .totalCost(p.getTotalCost())
                .retailPrice(p.getRetailPrice())
                .grade(p.getGrade())
                .notes(p.getNotes())
                .purchaseDate(p.getPurchaseDate())
                .resultingStock(resultingStock)
                .createdAt(p.getCreatedAt())
                .build();
    }
}
