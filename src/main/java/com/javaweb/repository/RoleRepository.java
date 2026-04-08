package com.javaweb.repository;

import com.javaweb.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    
    // Hàm này cực kỳ quan trọng để check quyền khi User đăng nhập
    // Ví dụ: findByName("ADMIN") hoặc findByName("USER")
    Optional<Role> findByName(String name);
}