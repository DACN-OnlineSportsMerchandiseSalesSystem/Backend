package com.javaweb.repository;

import com.javaweb.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    @Modifying
    @Transactional
    void deleteByProducts_Id(Long productId);

    java.util.Optional<ProductVariant> findBySkuCode(String skuCode);
}
