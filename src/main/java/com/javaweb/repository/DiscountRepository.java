package com.javaweb.repository;

import com.javaweb.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {

    /**
     * Lấy tất cả discounts đang active và còn trong thời hạn.
     * Điều kiện ngày: (startDate null HOẶC startDate <= now) VÀ (endDate null HOẶC endDate >= now)
     */
    @Query("SELECT d FROM Discount d WHERE d.isActive = true " +
           "AND (d.startDate IS NULL OR d.startDate <= :now) " +
           "AND (d.endDate IS NULL OR d.endDate >= :now)")
    List<Discount> findAllActiveDiscounts(@Param("now") Date now);

    @Modifying
    @Query("UPDATE Discount d SET d.category = null WHERE d.category.id = :categoryId")
    void nullifyCategoryReferences(@Param("categoryId") Long categoryId);
}
