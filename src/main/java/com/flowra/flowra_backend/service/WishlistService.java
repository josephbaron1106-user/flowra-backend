package com.flowra.flowra_backend.service;

import com.flowra.flowra_backend.dto.WishlistDTO;
import com.flowra.flowra_backend.entity.Product;
import com.flowra.flowra_backend.entity.ProductImage;
import com.flowra.flowra_backend.entity.User;
import com.flowra.flowra_backend.entity.Wishlist;
import com.flowra.flowra_backend.exception.BadRequestException;
import com.flowra.flowra_backend.exception.ResourceNotFoundException;
import com.flowra.flowra_backend.repository.ProductImageRepository;
import com.flowra.flowra_backend.repository.ProductRepository;
import com.flowra.flowra_backend.repository.UserRepository;
import com.flowra.flowra_backend.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    @Transactional
    public WishlistDTO.Response addToWishlist(WishlistDTO.Request request) {
        if (wishlistRepository.existsByUserIdAndProductId(request.getUserId(), request.getProductId())) {
            throw new BadRequestException("Product already in wishlist");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .product(product)
                .build();

        Wishlist saved = wishlistRepository.save(wishlist);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<WishlistDTO.Response> getWishlistByUser(Long userId) {
        return wishlistRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeFromWishlist(Long id) {
        if (!wishlistRepository.existsById(id)) {
            throw new ResourceNotFoundException("Wishlist item not found with id: " + id);
        }
        wishlistRepository.deleteById(id);
    }

    @Transactional
    public void removeFromWishlistByUserAndProduct(Long userId, Long productId) {
        wishlistRepository.deleteByUserIdAndProductId(userId, productId);
    }

    public WishlistDTO.Response mapToResponse(Wishlist wishlist) {
        Product product = wishlist.getProduct();
        String image = productImageRepository.findByProductIdOrderByDisplayOrderAsc(product.getId()).stream()
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElse(null);

        String categoryName = product.getCategory() != null ? product.getCategory().getName() : null;

        return WishlistDTO.Response.builder()
                .id(wishlist.getId())
                .userId(wishlist.getUser().getId())
                .productId(product.getId())
                .productName(product.getName())
                .productSlug(product.getSlug())
                .price(product.getPrice())
                .imageUrl(image)
                .category(categoryName)
                .createdAt(wishlist.getCreatedAt())
                .build();
    }
}
