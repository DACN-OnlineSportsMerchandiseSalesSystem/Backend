package com.javaweb.repository;

import com.javaweb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Spring sẽ tự hiểu: Thao tác trên bảng User, Khóa chính kiểu Long
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    
}