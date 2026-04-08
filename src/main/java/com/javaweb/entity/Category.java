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
    
    @Column(name = "name")
    private String name;

	// 1 Thương hiệu có nhiều Sản phẩm gốc (Product)
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "category") // Chữ 'b' viết thường
	private Set<Product> products = new HashSet<>(); // Đổi thành Product và có 's'
   

}
