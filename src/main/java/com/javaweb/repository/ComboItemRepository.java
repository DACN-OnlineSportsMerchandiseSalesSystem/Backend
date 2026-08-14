package com.javaweb.repository;

import com.javaweb.entity.ComboItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComboItemRepository extends JpaRepository<ComboItem, Long> {
    List<ComboItem> findByComboProductId(Long comboProductId);
}
