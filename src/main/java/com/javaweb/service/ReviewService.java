package com.javaweb.service;

import com.javaweb.dto.ReviewDTO;
import com.javaweb.dto.ReviewRequestDTO;

import java.util.List;

public interface ReviewService {
    ReviewDTO addReview(Long productId, ReviewRequestDTO request, String userEmail);
    List<ReviewDTO> getReviewsByProduct(Long productId);
    void deleteReview(Long reviewId, String userEmail);
    ReviewDTO replyToReview(Long reviewId, String adminReply);
    List<ReviewDTO> getAllReviews();
    List<ReviewDTO> getLatest5StarReviews();
}
