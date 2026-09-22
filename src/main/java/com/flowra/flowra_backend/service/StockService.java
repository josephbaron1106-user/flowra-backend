package com.flowra.flowra_backend.service;

import com.flowra.flowra_backend.dto.StockDTO;
import com.flowra.flowra_backend.entity.Flower;
import com.flowra.flowra_backend.entity.Stock;
import com.flowra.flowra_backend.exception.ResourceNotFoundException;
import com.flowra.flowra_backend.repository.FlowerRepository;
import com.flowra.flowra_backend.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final FlowerRepository flowerRepository;

    @Transactional(readOnly = true)
    public List<StockDTO.Response> getAllStock() {
        return stockRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StockDTO.Response getStockByFlowerId(Long flowerId) {
        Stock stock = stockRepository.findByFlowerId(flowerId)
                .orElseGet(() -> {
                    Flower flower = flowerRepository.findById(flowerId)
                            .orElseThrow(() -> new ResourceNotFoundException("Flower not found with id: " + flowerId));
                    return stockRepository.save(Stock.builder()
                            .flower(flower)
                            .totalInflow(0)
                            .totalOutflow(0)
                            .currentStock(0)
                            .minThreshold(10)
                            .build());
                });
        return mapToResponse(stock);
    }

    @Transactional(readOnly = true)
    public List<StockDTO.Response> getLowStock() {
        return stockRepository.findLowStock().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public StockDTO.Response updateMinThreshold(Long flowerId, Integer minThreshold) {
        Stock stock = stockRepository.findByFlowerId(flowerId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found for flower id: " + flowerId));
        if (minThreshold != null && minThreshold >= 0) {
            stock.setMinThreshold(minThreshold);
        }
        Stock saved = stockRepository.save(stock);
        return mapToResponse(saved);
    }

    public StockDTO.Response mapToResponse(Stock stock) {
        Flower flower = stock.getFlower();
        int current = stock.getCurrentStock() != null ? stock.getCurrentStock() : 0;
        int min = stock.getMinThreshold() != null ? stock.getMinThreshold() : 10;

        String status = "IN_STOCK";
        String statusLabel = "In Stock";
        if (current == 0) {
            status = "OUT_OF_STOCK";
            statusLabel = "Out of Stock";
        } else if (current <= min) {
            status = "LOW_STOCK";
            statusLabel = "Low Stock Alert";
        }

        return StockDTO.Response.builder()
                .id(stock.getId())
                .flowerId(flower.getId())
                .flowerName(flower.getName())
                .flowerSku(flower.getSku())
                .flowerSlug(flower.getSlug())
                .categoryName(flower.getCategory() != null ? flower.getCategory().getName() : "")
                .imageUrl(flower.getImageUrl())
                .retailPrice(flower.getPrice())
                .totalInflow(stock.getTotalInflow())
                .totalOutflow(stock.getTotalOutflow())
                .currentStock(current)
                .minThreshold(min)
                .status(status)
                .statusLabel(statusLabel)
                .lastRestockedAt(stock.getLastRestockedAt())
                .updatedAt(stock.getUpdatedAt())
                .build();
    }
}
