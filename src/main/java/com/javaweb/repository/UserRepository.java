package com.javaweb.repository;

import com.javaweb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Spring sẽ tự hiểu: Thao tác trên bảng User, Khóa chính kiểu Long
    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    // Thêm hàm tìm kiếm User bằng Email phục vụ đăng nhập
    Optional<User> findByEmail(String email);
}