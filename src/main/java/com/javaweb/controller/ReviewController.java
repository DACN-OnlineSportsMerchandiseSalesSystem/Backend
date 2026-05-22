package com.javaweb.controller;

import com.javaweb.dto.*;
import com.javaweb.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Review Management", description = "Endpoints for retrieving product reviews, posting customer reviews, deleting reviews, and admin review replies")
public class ReviewController {

    private final ReviewService reviewService;

    // Lấy Email từ hệ thống bảo vệ vòng ngoài (Token)
    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    // HTTP GET: Hiển thị danh sách bình luận của 1 Đôi Giày/Áo cụ thể
    @GetMapping("/products/{productId}/reviews")
    @Operation(summary = "Get reviews for a product", description = "Retrieve a list of all customer reviews and ratings associated with a specific product ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved reviews list")
    })
    public ResponseEntity<List<ReviewDTO>> getProductReviews(
            @Parameter(description = "ID of the product", example = "1", required = true)
            @PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getReviewsByProduct(productId));
    }

    // HTTP POST: Gửi lời bình luận kèm Số sao (1 đến 5)
    @PostMapping("/products/{productId}/reviews")
    @Operation(summary = "Submit a product review", description = "Submit a rating (1-5 stars) and comments on a purchased product under the user's active session.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Review submitted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid payload details or rating value out of bounds")
    })
    public ResponseEntity<ReviewDTO> addProductReview(
            @Parameter(description = "ID of the product being reviewed", example = "1", required = true)
            @PathVariable Long productId,
            @RequestBody ReviewRequestDTO request) {
        ReviewDTO review = reviewService.addReview(productId, request, getCurrentUserEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    // HTTP DELETE: Xóa bình luận (Dành cho bản thân người viết, hoặc Admin)
    @DeleteMapping("/reviews/{reviewId}")
    @Operation(summary = "Delete a review", description = "Permanently delete a product review from the database. Allowed only for the original author or admin.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Review deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Action not permitted for this user session"),
        @ApiResponse(responseCode = "404", description = "Review not found with the given ID")
    })
    public ResponseEntity<Void> deleteReview(
            @Parameter(description = "ID of the review to delete", example = "1", required = true)
            @PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId, getCurrentUserEmail());
        return ResponseEntity.noContent().build();
    }

    // HTTP GET: Admin lấy tất cả đánh giá
    @GetMapping("/reviews")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all store reviews", description = "Admin only. Retrieve a comprehensive list of all product reviews submitted across the store.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved reviews list"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN authority")
    })
    public ResponseEntity<List<ReviewDTO>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    @GetMapping("/reviews/latest-5-star")
    @Operation(summary = "Get latest 5-star reviews", description = "Retrieve the 3 most recent 5-star reviews across the store.")
    public ResponseEntity<List<ReviewDTO>> getLatest5StarReviews() {
        return ResponseEntity.ok(reviewService.getLatest5StarReviews());
    }

    // HTTP PUT: Admin phản hồi đánh giá của khách hàng
    @PutMapping("/reviews/{reviewId}/reply")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reply to a review", description = "Admin only. Add or update the official administrative reply text for a customer review.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reply saved successfully"),
        @ApiResponse(responseCode = "400", description = "Missing reply content"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN authority"),
        @ApiResponse(responseCode = "404", description = "Review not found with the given ID")
    })
    public ResponseEntity<ReviewDTO> replyToReview(
            @Parameter(description = "ID of the customer review being replied to", example = "1", required = true)
            @PathVariable Long reviewId,
            @RequestBody ReviewReplyRequestDTO payload) {
        String adminReply = payload.getAdminReply();
        if (adminReply == null || adminReply.trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung phản hồi không được để trống!");
        }
        return ResponseEntity.ok(reviewService.replyToReview(reviewId, adminReply));
    }
}
