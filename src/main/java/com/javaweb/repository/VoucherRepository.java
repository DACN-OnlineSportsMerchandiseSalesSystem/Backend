package com.javaweb.repository;

import com.javaweb.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByCode(String code);

    @Query("SELECT v FROM Voucher v WHERE v.expiryDate > CURRENT_TIMESTAMP AND (v.usageLimit IS NULL OR v.usedCount < v.usageLimit)")
    List<Voucher> findValidVouchers();
}
