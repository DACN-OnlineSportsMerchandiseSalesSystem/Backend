package com.javaweb.repository;

import com.javaweb.entity.Cart;
import com.javaweb.enums.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Cart> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, CartStatus status);
    Optional<Cart> findByIdAndUserId(Long id, Long userId);
    Optional<Cart> findFirstByUserIdAndIsDefaultTrueAndStatus(Long userId, CartStatus status);
    Optional<Cart> findFirstByUserIdAndStatusOrderByCreatedAtAsc(Long userId, CartStatus status);
    List<Cart> findByUserIdAndStatus(Long userId, CartStatus status);
}
