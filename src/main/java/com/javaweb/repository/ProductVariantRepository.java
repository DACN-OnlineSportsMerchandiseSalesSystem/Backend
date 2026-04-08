package com.javaweb.repository;

import com.javaweb.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

public interface ProductVariantRepository extends JpaRepository<ProductVariant,Long>{

}
