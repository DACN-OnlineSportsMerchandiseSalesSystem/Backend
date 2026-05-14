package com.javaweb.repository;

import com.javaweb.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    @Modifying
    @Transactional
    void deleteByProducts_Id(Long productId);
}
