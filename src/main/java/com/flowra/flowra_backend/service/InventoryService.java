package com.flowra.flowra_backend.service;

import com.flowra.flowra_backend.dto.InventoryDTO;
import com.flowra.flowra_backend.entity.Inventory;
import com.flowra.flowra_backend.entity.Product;
import com.flowra.flowra_backend.exception.ResourceNotFoundException;
import com.flowra.flowra_backend.repository.InventoryRepository;
import com.flowra.flowra_backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<InventoryDTO.Response> getAllInventory() {
        return inventoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InventoryDTO.Response> getLowStockInventory() {
        return inventoryRepository.findLowStockInventory().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public InventoryDTO.Response getInventoryByProductId(Long productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseGet(() -> {
                    Product product = productRepository.findById(productId)
                            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
                    Inventory newInv = Inventory.builder()
                            .product(product)
                            .currentStock(0)
                            .reservedStock(0)
                            .minThreshold(10)
                            .build();
                    return inventoryRepository.save(newInv);
                });
        return mapToResponse(inventory);
    }

    @Transactional
    public InventoryDTO.Response updateStock(Long productId, InventoryDTO.UpdateRequest request) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseGet(() -> {
                    Product product = productRepository.findById(productId)
                            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
                    return Inventory.builder().product(product).build();
                });

        if (request.getDelta() != null) {
            int newStock = Math.max(0, inventory.getCurrentStock() + request.getDelta());
            inventory.setCurrentStock(newStock);
        } else if (request.getStock() != null) {
            inventory.setCurrentStock(Math.max(0, request.getStock()));
        }

        if (request.getReservedStock() != null) {
            inventory.setReservedStock(Math.max(0, request.getReservedStock()));
        }
        if (request.getMinThreshold() != null) {
            inventory.setMinThreshold(Math.max(0, request.getMinThreshold()));
        }

        inventory.setLastRestockedAt(LocalDateTime.now());
        Inventory saved = inventoryRepository.save(inventory);
        return mapToResponse(saved);
    }

    public InventoryDTO.Response mapToResponse(Inventory inventory) {
        Product product = inventory.getProduct();
        boolean isLow = inventory.getCurrentStock() <= inventory.getMinThreshold();
        return InventoryDTO.Response.builder()
                .id(inventory.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productSku(product.getSku())
                .currentStock(inventory.getCurrentStock())
                .reservedStock(inventory.getReservedStock())
                .minThreshold(inventory.getMinThreshold())
                .isLowStock(isLow)
                .lastRestockedAt(inventory.getLastRestockedAt())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}
