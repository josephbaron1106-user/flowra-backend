package com.flowra.flowra_backend.service;

import com.flowra.flowra_backend.dto.ProductDTO;
import com.flowra.flowra_backend.entity.Category;
import com.flowra.flowra_backend.entity.Inventory;
import com.flowra.flowra_backend.entity.Product;
import com.flowra.flowra_backend.entity.ProductImage;
import com.flowra.flowra_backend.exception.BadRequestException;
import com.flowra.flowra_backend.exception.ResourceNotFoundException;
import com.flowra.flowra_backend.repository.CategoryRepository;
import com.flowra.flowra_backend.repository.InventoryRepository;
import com.flowra.flowra_backend.repository.ProductImageRepository;
import com.flowra.flowra_backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductImageRepository productImageRepository;

    @Transactional
    public ProductDTO.Response createProduct(ProductDTO.Request request) {
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
        }

        String sku = request.getSku();
        if (sku == null || sku.isBlank()) {
            sku = "SKU-" + System.currentTimeMillis();
        } else if (productRepository.existsBySku(sku)) {
            throw new BadRequestException("Product already exists with SKU: " + sku);
        }

        String slug = request.getSlug();
        if (slug == null || slug.isBlank()) {
            slug = request.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-") + "-" + System.currentTimeMillis() % 10000;
        }

        Product product = Product.builder()
                .category(category)
                .name(request.getName())
                .slug(slug)
                .sku(sku)
                .flowerType(request.getFlowerType())
                .color(request.getColor())
                .size(request.getSize())
                .price(request.getPrice())
                .originalPrice(request.getOriginalPrice())
                .discountPercent(request.getDiscountPercent() != null ? request.getDiscountPercent() : 0)
                .description(request.getDescription())
                .careInstructions(request.getCareInstructions())
                .popularity(request.getPopularity() != null ? request.getPopularity() : 0)
                .status(request.getStatus() != null ? request.getStatus() : "Active")
                .isFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false)
                .build();

        Product savedProduct = productRepository.save(product);

        // Create Inventory record
        int initialStock = request.getInitialStock() != null ? request.getInitialStock() : 20;
        Inventory inventory = Inventory.builder()
                .product(savedProduct)
                .currentStock(initialStock)
                .reservedStock(0)
                .minThreshold(5)
                .build();
        inventoryRepository.save(inventory);

        // Save images
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            for (int i = 0; i < request.getImageUrls().size(); i++) {
                ProductImage image = ProductImage.builder()
                        .product(savedProduct)
                        .imageUrl(request.getImageUrls().get(i))
                        .isPrimary(i == 0)
                        .displayOrder(i)
                        .build();
                productImageRepository.save(image);
            }
        }

        return mapToResponse(savedProduct);
    }

    @Transactional(readOnly = true)
    public List<ProductDTO.Response> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDTO.Response> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDTO.Response> getFeaturedProducts() {
        return productRepository.findByIsFeaturedTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductDTO.Response getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return mapToResponse(product);
    }

    @Transactional
    public ProductDTO.Response updateProduct(Long id, ProductDTO.Request request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
            product.setCategory(category);
        }

        if (request.getName() != null) product.setName(request.getName());
        if (request.getFlowerType() != null) product.setFlowerType(request.getFlowerType());
        if (request.getColor() != null) product.setColor(request.getColor());
        if (request.getSize() != null) product.setSize(request.getSize());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getOriginalPrice() != null) product.setOriginalPrice(request.getOriginalPrice());
        if (request.getDiscountPercent() != null) product.setDiscountPercent(request.getDiscountPercent());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getCareInstructions() != null) product.setCareInstructions(request.getCareInstructions());
        if (request.getStatus() != null) product.setStatus(request.getStatus());
        if (request.getIsFeatured() != null) product.setIsFeatured(request.getIsFeatured());

        Product updated = productRepository.save(product);

        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            productImageRepository.deleteByProductId(id);
            for (int i = 0; i < request.getImageUrls().size(); i++) {
                ProductImage image = ProductImage.builder()
                        .product(updated)
                        .imageUrl(request.getImageUrls().get(i))
                        .isPrimary(i == 0)
                        .displayOrder(i)
                        .build();
                productImageRepository.save(image);
            }
        }

        return mapToResponse(updated);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    public ProductDTO.Response mapToResponse(Product product) {
        List<ProductImage> images = productImageRepository.findByProductIdOrderByDisplayOrderAsc(product.getId());
        List<String> imageUrls = images.stream().map(ProductImage::getImageUrl).collect(Collectors.toList());
        String primaryImage = images.stream()
                .filter(ProductImage::getIsPrimary)
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElse(imageUrls.isEmpty() ? null : imageUrls.get(0));

        Integer currentStock = inventoryRepository.findByProductId(product.getId())
                .map(Inventory::getCurrentStock)
                .orElse(0);

        return ProductDTO.Response.builder()
                .id(product.getId())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .name(product.getName())
                .slug(product.getSlug())
                .sku(product.getSku())
                .flowerType(product.getFlowerType())
                .color(product.getColor())
                .size(product.getSize())
                .price(product.getPrice())
                .originalPrice(product.getOriginalPrice())
                .discountPercent(product.getDiscountPercent())
                .description(product.getDescription())
                .careInstructions(product.getCareInstructions())
                .popularity(product.getPopularity())
                .status(product.getStatus())
                .isFeatured(product.getIsFeatured())
                .currentStock(currentStock)
                .images(imageUrls)
                .primaryImage(primaryImage)
                .createdAt(product.getCreatedAt())
                .build();
    }
}
