package com.javaweb.repository;

import com.javaweb.entity.StorePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StorePolicyRepository extends JpaRepository<StorePolicy, Long> {
    List<StorePolicy> findByIsVectorizedFalse();
    List<StorePolicy> findByIsActiveTrueOrderByDisplayOrderAsc();
    Optional<StorePolicy> findByPolicyKey(String policyKey);
}
