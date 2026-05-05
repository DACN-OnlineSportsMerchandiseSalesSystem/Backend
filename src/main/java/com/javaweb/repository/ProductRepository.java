package com.javaweb.repository;

import com.javaweb.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Bạn có thể viết thêm các hàm tìm kiếm cực nhanh ở đây
    List<Product> findByNameContaining(String name); // Tự động tạo SQL: WHERE name LIKE %name%
    
    // Lọc theo Category và Brand
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByBrandId(Long brandId);
    List<Product> findByCategoryIdAndBrandId(Long categoryId, Long brandId);
    List<Product> findByIsVectorizedFalse();
}