package com.javaweb.repository;

import com.javaweb.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Long> {
    List<Orders> findByUserId(Long userId);

    @Query("SELECT o FROM Orders o WHERE " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:start IS NULL OR o.createAt >= :start) AND " +
           "(:end IS NULL OR o.createAt <= :end) AND " +
           "(:keyword IS NULL OR " +
           "LOWER(o.receiverName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(o.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(o.user.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(o.user.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(o.user.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Orders> findWithFilters(@Param("status") String status, 
                                 @Param("start") Date start, 
                                 @Param("end") Date end, 
                                 @Param("keyword") String keyword);
    @Query("SELECT SUM(o.totalPrice) FROM Orders o WHERE o.status = 'PAID' AND o.createAt >= :start AND o.createAt < :end")
    BigDecimal sumRevenueByDateRange(@Param("start") Date start, @Param("end") Date end);

    @Query("SELECT COUNT(o) FROM Orders o WHERE o.createAt >= :start AND o.createAt < :end")
    Integer countOrdersByDateRange(@Param("start") Date start, @Param("end") Date end);
}
