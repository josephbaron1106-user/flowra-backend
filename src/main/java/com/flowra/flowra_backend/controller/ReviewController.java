package com.flowra.flowra_backend.controller;

import com.flowra.flowra_backend.dto.ReviewDTO;
import com.flowra.flowra_backend.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewDTO.Response> createReview(@Valid @RequestBody ReviewDTO.Request request) {
        return new ResponseEntity<>(reviewService.createReview(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ReviewDTO.Response>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewDTO.Response>> getReviewsByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getReviewsByProduct(productId));
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<ReviewDTO.Response> toggleReview(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.toggleReviewStatus(id));
    }

    @PutMapping("/{id}/reply")
    public ResponseEntity<ReviewDTO.Response> replyToReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewDTO.AdminReplyRequest request) {
        return ResponseEntity.ok(reviewService.addAdminReply(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}
