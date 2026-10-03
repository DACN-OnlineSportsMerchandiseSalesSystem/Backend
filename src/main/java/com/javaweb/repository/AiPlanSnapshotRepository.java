package com.javaweb.repository;

import com.javaweb.entity.AiPlanSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiPlanSnapshotRepository extends JpaRepository<AiPlanSnapshot, Long> {

    List<AiPlanSnapshot> findByUserEmailOrderByCreatedAtDesc(String userEmail);

    List<AiPlanSnapshot> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<AiPlanSnapshot> findByIdAndUserEmail(Long id, String userEmail);

    Optional<AiPlanSnapshot> findFirstByUserEmailAndIsActiveTrueOrderByUpdatedAtDesc(String userEmail);
}
