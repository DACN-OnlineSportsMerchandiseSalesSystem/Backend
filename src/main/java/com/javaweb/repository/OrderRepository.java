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

    @Query("SELECT SUM(o.totalPrice) FROM Orders o WHERE o.status = 'PAID' AND o.createAt >= :start AND o.createAt < :end")
    BigDecimal sumRevenueByDateRange(@Param("start") Date start, @Param("end") Date end);

    @Query("SELECT COUNT(o) FROM Orders o WHERE o.createAt >= :start AND o.createAt < :end")
    Integer countOrdersByDateRange(@Param("start") Date start, @Param("end") Date end);
}
