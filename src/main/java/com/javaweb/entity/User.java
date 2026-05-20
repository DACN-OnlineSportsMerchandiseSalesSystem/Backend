package com.javaweb.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import com.javaweb.enums.*;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private UserStatus status;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "password")
    private String password;

    @Column(name = "level")
    private Long level; // Dùng để tích điểm thành viên

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender; 

    @Enumerated(EnumType.STRING)
<<<<<<< HEAD
    @Column(name = "`rank`")
=======
    @Column(name = "ranked")
>>>>>>> 0e1e55ba18976b5c12b987bc76b34759271984c4
    private RankType rank; 

    @Column(name = "last_login")
    @UpdateTimestamp
    private Date lastLogin;

    @Column(name = "created_at")
    @CreationTimestamp
    private Date createdAt;

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

    // 6. Danh mục quan tâm (Many-to-Many)
    @ManyToMany
    @JoinTable(
        name = "user_category_interest",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> interestedCategories = new HashSet<>();
}