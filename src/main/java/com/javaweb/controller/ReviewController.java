package com.javaweb.controller;

import com.javaweb.dto.*;
import com.javaweb.service.ReviewService;
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
public class ReviewController {

    private final ReviewService reviewService;

    // Lấy Email từ hệ thống bảo vệ vòng ngoài (Token)
    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    // HTTP GET: Hiển thị danh sách bình luận của 1 Đôi Giày/Áo cụ thể
    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<List<ReviewDTO>> getProductReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getReviewsByProduct(productId));
    }

    // HTTP POST: Gửi lời bình luận kèm Số sao (1 đến 5)
    @PostMapping("/products/{productId}/reviews")
    public ResponseEntity<ReviewDTO> addProductReview(
            @PathVariable Long productId,
            @RequestBody ReviewRequestDTO request) {
        ReviewDTO review = reviewService.addReview(productId, request, getCurrentUserEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    // HTTP DELETE: Xóa bình luận (Dành cho bản thân người viết, hoặc Admin)
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId, getCurrentUserEmail());
        return ResponseEntity.noContent().build();
    }

    // HTTP PUT: Admin phản hồi đánh giá của khách hàng
    @PutMapping("/reviews/{reviewId}/reply")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ReviewDTO> replyToReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewReplyRequestDTO payload) {
        String adminReply = payload.getAdminReply();
        if (adminReply == null || adminReply.trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung phản hồi không được để trống!");
        }
        return ResponseEntity.ok(reviewService.replyToReview(reviewId, adminReply));
    }
}
