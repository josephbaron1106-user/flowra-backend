package com.flowra.flowra_backend.service;

import com.flowra.flowra_backend.dto.ReviewDTO;
import com.flowra.flowra_backend.entity.Product;
import com.flowra.flowra_backend.entity.Review;
import com.flowra.flowra_backend.entity.User;
import com.flowra.flowra_backend.exception.ResourceNotFoundException;
import com.flowra.flowra_backend.repository.ProductRepository;
import com.flowra.flowra_backend.repository.ReviewRepository;
import com.flowra.flowra_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReviewDTO.Response createReview(ReviewDTO.Request request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId()).orElse(null);
        }

        Review review = Review.builder()
                .product(product)
                .user(user)
                .authorName(request.getAuthorName().trim())
                .rating(request.getRating())
                .comment(request.getComment().trim())
                .status("Approved")
                .build();

        Review saved = reviewRepository.save(review);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ReviewDTO.Response> getReviewsByProduct(Long productId) {
        return reviewRepository.findByProductId(productId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReviewDTO.Response addAdminReply(Long reviewId, ReviewDTO.AdminReplyRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        review.setAdminReply(request.getAdminReply());
        review.setAdminRepliedAt(LocalDateTime.now());
        Review updated = reviewRepository.save(review);
        return mapToResponse(updated);
    }

    @Transactional(readOnly = true)
    public List<ReviewDTO.Response> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReviewDTO.Response toggleReviewStatus(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));
        review.setStatus("Approved".equalsIgnoreCase(review.getStatus()) ? "Hidden" : "Approved");
        return mapToResponse(reviewRepository.save(review));
    }

    @Transactional
    public void deleteReview(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new ResourceNotFoundException("Review not found with id: " + id);
        }
        reviewRepository.deleteById(id);
    }

    public ReviewDTO.Response mapToResponse(Review review) {
        return ReviewDTO.Response.builder()
                .id(review.getId())
                .userId(review.getUser() != null ? review.getUser().getId() : null)
                .productId(review.getProduct().getId())
                .productName(review.getProduct().getName())
                .authorName(review.getAuthorName())
                .rating(review.getRating())
                .comment(review.getComment())
                .status(review.getStatus())
                .adminReply(review.getAdminReply())
                .adminRepliedAt(review.getAdminRepliedAt())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
