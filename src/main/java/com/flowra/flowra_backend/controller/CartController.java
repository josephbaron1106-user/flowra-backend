package com.flowra.flowra_backend.controller;

import com.flowra.flowra_backend.dto.CartDTO;
import com.flowra.flowra_backend.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<CartDTO.CartResponse> getCartByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(cartService.getCartByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<CartDTO.CartResponse> addToCart(@Valid @RequestBody CartDTO.AddItemRequest request) {
        return ResponseEntity.ok(cartService.addItemToCart(request));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<CartDTO.CartResponse> updateCartItem(
            @PathVariable Long id,
            @Valid @RequestBody CartDTO.UpdateItemRequest request) {
        return ResponseEntity.ok(cartService.updateCartItemQuantity(id, request));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> removeCartItem(@PathVariable Long id) {
        cartService.removeCartItem(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
