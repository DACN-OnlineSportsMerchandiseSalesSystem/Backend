package com.javaweb.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "categories") // Đã thêm 's' cho khớp với Database
@Getter
@Setter

public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;
    
    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name = "slug")
    private String slug;

    @Column(name = "status")
    private String status;

    @Column(name = "discount")
    private Integer discount;

    @Column(name = "rating")
    private Integer rating;

    
    // 1 Thương hiệu có nhiều Sản phẩm gốc (Product)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "category") // Chữ 'b' viết thường
    private Set<Product> products = new HashSet<>(); // Đổi thành Product và có 's'
   

}
