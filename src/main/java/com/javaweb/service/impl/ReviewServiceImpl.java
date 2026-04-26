package com.javaweb.service.impl;

import com.javaweb.dto.ReviewDTO;
import com.javaweb.dto.ReviewRequestDTO;
import com.javaweb.entity.Product;
import com.javaweb.entity.Review;
import com.javaweb.entity.User;
import com.javaweb.exception.ResouceNotFoundException;
import com.javaweb.repository.ProductRepository;
import com.javaweb.repository.ReviewRepository;
import com.javaweb.repository.UserRepository;
import com.javaweb.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public ReviewDTO addReview(Long productId, ReviewRequestDTO request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResouceNotFoundException("User not found with email: " + userEmail));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResouceNotFoundException("Product not found with id: " + productId));

        Review review = new Review();
        review.setUser(user);
        review.setProducts(product);
        review.setContent(request.getContent());
        review.setRating(request.getRating() != null ? request.getRating() : 5);

        return mapToDTO(reviewRepository.save(review));
    }

    @Override
    public List<ReviewDTO> getReviewsByProduct(Long productId) {
        return reviewRepository.findByProductsId(productId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteReview(Long reviewId, String userEmail) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResouceNotFoundException("Review not found with id: " + reviewId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResouceNotFoundException("User not found"));

        boolean isAdmin = user.getRole() != null && user.getRole().getName().equalsIgnoreCase("ADMIN");
        boolean isOwner = review.getUser().getId().equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new RuntimeException("Xin lỗi, bạn chỉ có thể xóa bình luận của chính mình!");
        }

        reviewRepository.delete(review);
    }

    private ReviewDTO mapToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setContent(review.getContent());
        dto.setRating(review.getRating());

        if (review.getUser() != null) {
            dto.setUserName(review.getUser().getLastName() + " " + review.getUser().getFirstName());
        }
        if (review.getProducts() != null) {
            dto.setProductId(review.getProducts().getId());
        }

        return dto;
    }
}
