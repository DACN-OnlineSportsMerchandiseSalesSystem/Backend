package com.javaweb.repository;

import com.javaweb.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Date;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import com.javaweb.dto.TopSellingProductDTO;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Bạn có thể viết thêm các hàm tìm kiếm cực nhanh ở đây
    List<Product> findByNameContaining(String name); // Tự động tạo SQL: WHERE name LIKE %name%

    // Lọc theo Category và Brand
    List<Product> findByCategories_Id(Long categoryId);
<<<<<<< HEAD
    List<Product> findByCategories_IdIn(List<Long> categoryIds, org.springframework.data.domain.Pageable pageable);
=======
    
    List<Product> findByCategories_IdIn(List<Long> categoryIds);
>>>>>>> 0e1e55ba18976b5c12b987bc76b34759271984c4

    List<Product> findByBrandId(Long brandId);

    List<Product> findByCategories_IdAndBrandId(Long categoryId, Long brandId);

    @Query("SELECT new com.javaweb.dto.TopSellingProductDTO(p.id, p.name, SUM(oi.quantity), SUM(oi.quantity * oi.priceAtPurchase)) "
            +
            "FROM OrderItems oi " +
            "JOIN oi.productVariants pv " +
            "JOIN pv.products p " +
            "JOIN oi.orders o " +
            "WHERE o.status = com.javaweb.enums.OrderStatus.PAID AND o.createAt >= :start AND o.createAt < :end " +
            "GROUP BY p.id, p.name " +
            "ORDER BY SUM(oi.quantity) DESC")
    List<TopSellingProductDTO> getTopSellingProducts(
            @Param("start") Date start,
            @Param("end") Date end,
            Pageable pageable);

    List<Product> findByIsVectorizedFalse();

    @Query("SELECT p.id " +
            "FROM OrderItems oi " +
            "JOIN oi.productVariants pv " +
            "JOIN pv.products p " +
            "JOIN oi.orders o " +
            "WHERE o.status IN (com.javaweb.enums.OrderStatus.PAID, com.javaweb.enums.OrderStatus.COMPLETED) " +
            "GROUP BY p.id " +
            "ORDER BY SUM(oi.quantity) DESC")
    List<Long> findTopSellingProductIds(Pageable pageable);
}