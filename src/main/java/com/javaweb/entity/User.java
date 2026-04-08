package com.javaweb.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp; // Đã bỏ comment

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "phone")
    private String phone; // Đã sửa thành String để không mất số 0
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "status")
    private String status; // Đã sửa thành String (ACTIVE, BANNED, LOCKED)
    
    @Column(name = "full_name")
    private String fullName;
    
    @Column(name = "password")
    private String password;
    
    @Column(name = "is_super_admin") // Đã xóa dấu chấm phẩy ;
    private Boolean isSuperAdmin;
    
    @Column(name = "level")
    private String level; // Nên dùng String (vd: Quản lý, Nhân viên)
    
    @Column(name = "last_login")
    @UpdateTimestamp
    private Date lastLogin;

    // ================= CÁC MỐI QUAN HỆ =================

    // 1. Phân quyền (Nhiều User xài chung 1 Role)
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role; // Nhớ tạo file Role.java

    // 2. Sổ địa chỉ (1 User có nhiều Address)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private Set<Address> addresses = new HashSet<>();

    // 3. Đơn hàng (1 User có nhiều Order)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private Set<Orders> orders = new HashSet<>();

    // 4. Giỏ hàng (1 User chỉ có 1 Cart)
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Cart cart; // Nhớ tạo file Cart.java

    // 5. Đánh giá (1 User viết nhiều Review)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private Set<Review> reviews = new HashSet<>();
}