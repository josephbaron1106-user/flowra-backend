package com.flowra.flowra_backend.service;

import com.flowra.flowra_backend.dto.CartDTO;
import com.flowra.flowra_backend.entity.Cart;
import com.flowra.flowra_backend.entity.CartItem;
import com.flowra.flowra_backend.entity.Product;
import com.flowra.flowra_backend.entity.ProductImage;
import com.flowra.flowra_backend.entity.User;
import com.flowra.flowra_backend.exception.ResourceNotFoundException;
import com.flowra.flowra_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    @Transactional
    public Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
                    Cart newCart = Cart.builder().user(user).build();
                    return cartRepository.save(newCart);
                });
    }

    @Transactional
    public CartDTO.CartResponse addItemToCart(CartDTO.AddItemRequest request) {
        Cart cart = getOrCreateCart(request.getUserId());
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        Optional<CartItem> existingItemOpt = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());
        if (existingItemOpt.isPresent()) {
            CartItem item = existingItemOpt.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            cartItemRepository.save(item);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cartItemRepository.save(newItem);
        }

        return getCartByUserId(request.getUserId());
    }

    @Transactional
    public CartDTO.CartResponse getCartByUserId(Long userId) {
        Cart cart = getOrCreateCart(userId);
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());

        List<CartDTO.ItemResponse> itemResponses = items.stream().map(item -> {
            Product p = item.getProduct();
            BigDecimal subtotal = p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

            String image = productImageRepository.findByProductIdOrderByDisplayOrderAsc(p.getId()).stream()
                    .map(ProductImage::getImageUrl)
                    .findFirst()
                    .orElse(null);

            return CartDTO.ItemResponse.builder()
                    .id(item.getId())
                    .productId(p.getId())
                    .productName(p.getName())
                    .productSlug(p.getSlug())
                    .price(p.getPrice())
                    .imageUrl(image)
                    .quantity(item.getQuantity())
                    .subtotal(subtotal)
                    .build();
        }).collect(Collectors.toList());

        int totalItems = itemResponses.stream().mapToInt(CartDTO.ItemResponse::getQuantity).sum();
        BigDecimal totalAmount = itemResponses.stream()
                .map(CartDTO.ItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartDTO.CartResponse.builder()
                .cartId(cart.getId())
                .userId(userId)
                .items(itemResponses)
                .totalItems(totalItems)
                .totalAmount(totalAmount)
                .build();
    }

    @Transactional
    public CartDTO.CartResponse updateCartItemQuantity(Long itemId, CartDTO.UpdateItemRequest request) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));

        if (request.getQuantity() <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(request.getQuantity());
            cartItemRepository.save(item);
        }

        return getCartByUserId(item.getCart().getUser().getId());
    }

    @Transactional
    public void removeCartItem(Long itemId) {
        if (!cartItemRepository.existsById(itemId)) {
            throw new ResourceNotFoundException("Cart item not found with id: " + itemId);
        }
        cartItemRepository.deleteById(itemId);
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cartItemRepository.deleteByCartId(cart.getId());
    }
}
