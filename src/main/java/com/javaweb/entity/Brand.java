package com.javaweb.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "brands") // Đã thêm 's' cho khớp với Database
@Getter
@Setter
public class Brand {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
	
    @Column(name = "name")
    private String name;
	
    @Column(name = "detail", columnDefinition = "TEXT") // Thêm TEXT cho nội dung dài
    private String detail;
	
    @Column(name = "url")
    private String url;
    
    @Column(name = "image")
    private String image;
    
    // 1 Thương hiệu có nhiều Sản phẩm gốc (Product)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "brand") // Chữ 'b' viết thường
    private Set<Product> products = new HashSet<>(); // Đổi thành Product và có 's'
	
}