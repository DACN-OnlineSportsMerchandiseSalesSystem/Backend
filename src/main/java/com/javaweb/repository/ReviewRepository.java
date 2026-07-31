package com.javaweb.repository;

import com.javaweb.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    // Kéo toàn bộ bình luận của một sản phẩm
    List<Review> findByProductsId(Long productId);

    // Lấy 3 đánh giá có rating cụ thể mới nhất (Id giảm dần)
    List<Review> findTop3ByRatingOrderByIdDesc(int rating);
}
