package com.flowra.flowra_backend.service;

import com.flowra.flowra_backend.dto.FlowerDTO;
import com.flowra.flowra_backend.entity.Category;
import com.flowra.flowra_backend.entity.Flower;
import com.flowra.flowra_backend.entity.Stock;
import com.flowra.flowra_backend.exception.BadRequestException;
import com.flowra.flowra_backend.exception.ResourceNotFoundException;
import com.flowra.flowra_backend.repository.CategoryRepository;
import com.flowra.flowra_backend.repository.FlowerRepository;
import com.flowra.flowra_backend.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlowerService {

    private final FlowerRepository flowerRepository;
    private final CategoryRepository categoryRepository;
    private final StockRepository stockRepository;

    @Transactional(readOnly = true)
    public List<FlowerDTO.Response> getAllFlowers() {
        return flowerRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FlowerDTO.Response getFlowerById(Long id) {
        Flower flower = flowerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flower not found with id: " + id));
        return mapToResponse(flower);
    }

    @Transactional
    public FlowerDTO.Response createFlower(FlowerDTO.Request request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BadRequestException("Flower name is mandatory");
        }

        String name = request.getName().trim();
        if (flowerRepository.findByNameIgnoreCase(name).isPresent()) {
            throw new BadRequestException("A flower with name '" + name + "' already exists");
        }

        // Category resolution
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId()).orElse(null);
        }
        if (category == null && request.getCategoryName() != null && !request.getCategoryName().trim().isEmpty()) {
            String catName = request.getCategoryName().trim();
            category = categoryRepository.findByNameIgnoreCase(catName)
                    .orElseGet(() -> {
                        String catSlug = catName.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
                        Category newCat = Category.builder()
                                .name(catName)
                                .slug(catSlug.isEmpty() ? "category-" + System.currentTimeMillis() : catSlug)
                                .description("Catalog Category for " + catName)
                                .status("ACTIVE")
                                .displayOrder(10)
                                .build();
                        return categoryRepository.save(newCat);
                    });
        }
        if (category == null) {
            category = categoryRepository.findAll().stream().findFirst()
                    .orElseGet(() -> categoryRepository.save(Category.builder().name("Roses").slug("roses").build()));
        }

        // Slug generation
        String baseSlug = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
        String slug = baseSlug.isEmpty() ? "flower-" + System.currentTimeMillis() : baseSlug;
        if (flowerRepository.findBySlug(slug).isPresent()) {
            slug = slug + "-" + (int)(Math.random() * 900 + 100);
        }

        // SKU generation
        String sku = request.getSku();
        if (sku == null || sku.trim().isEmpty()) {
            String prefix = name.length() >= 3 ? name.substring(0, 3).toUpperCase(Locale.ROOT) : "FLW";
            sku = prefix + "-" + (int)(Math.random() * 9000 + 1000);
        }
        if (flowerRepository.findBySku(sku).isPresent()) {
            sku = sku + "-" + (int)(Math.random() * 90 + 10);
        }

        BigDecimal price = request.getPrice() != null && request.getPrice().compareTo(BigDecimal.ZERO) > 0
                ? request.getPrice() : BigDecimal.valueOf(50.00);

        Flower flower = Flower.builder()
                .name(name)
                .slug(slug)
                .sku(sku)
                .category(category)
                .price(price)
                .imageUrl(request.getImageUrl())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .build();

        Flower saved = flowerRepository.save(flower);

        // Initialize Stock record
        Stock stock = Stock.builder()
                .flower(saved)
                .totalInflow(0)
                .totalOutflow(0)
                .currentStock(0)
                .minThreshold(10)
                .lastRestockedAt(LocalDateTime.now())
                .build();
        stockRepository.save(stock);

        return mapToResponse(saved);
    }

    public FlowerDTO.Response mapToResponse(Flower flower) {
        Integer available = stockRepository.findByFlowerId(flower.getId())
                .map(Stock::getCurrentStock)
                .orElse(0);

        return FlowerDTO.Response.builder()
                .id(flower.getId())
                .name(flower.getName())
                .slug(flower.getSlug())
                .sku(flower.getSku())
                .price(flower.getPrice())
                .imageUrl(flower.getImageUrl())
                .status(flower.getStatus())
                .categoryId(flower.getCategory() != null ? flower.getCategory().getId() : null)
                .categoryName(flower.getCategory() != null ? flower.getCategory().getName() : "Uncategorized")
                .availableStock(available)
                .createdAt(flower.getCreatedAt())
                .build();
    }
}
