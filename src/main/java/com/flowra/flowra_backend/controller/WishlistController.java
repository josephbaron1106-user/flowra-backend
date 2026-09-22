package com.flowra.flowra_backend.controller;

import com.flowra.flowra_backend.dto.WishlistDTO;
import com.flowra.flowra_backend.service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<WishlistDTO.Response>> getWishlistByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(wishlistService.getWishlistByUser(userId));
    }

    @PostMapping
    public ResponseEntity<WishlistDTO.Response> addToWishlist(@Valid @RequestBody WishlistDTO.Request request) {
        return new ResponseEntity<>(wishlistService.addToWishlist(request), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeFromWishlist(@PathVariable Long id) {
        wishlistService.removeFromWishlist(id);
        return ResponseEntity.noContent().build();
    }
}
