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

        @org.springframework.data.jpa.repository.Query("SELECT new com.javaweb.dto.TopSellingProductDTO(p.id, p.name, SUM(oi.quantity), SUM(oi.quantity * oi.priceAtPurchase)) "
                        +
                        "FROM OrderItems oi " +
                        "JOIN oi.productVariants pv " +
                        "JOIN pv.products p " +
                        "JOIN oi.orders o " +
                        "WHERE o.status = 'PAID' AND o.createAt >= :start AND o.createAt < :end " +
                        "GROUP BY p.id, p.name " +
                        "ORDER BY SUM(oi.quantity) DESC")
        java.util.List<com.javaweb.dto.TopSellingProductDTO> getTopSellingProducts(
                        @org.springframework.data.repository.query.Param("start") java.util.Date start,
                        @org.springframework.data.repository.query.Param("end") java.util.Date end,
                        org.springframework.data.domain.Pageable pageable);
}