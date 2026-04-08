package com.javaweb.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "products") // Tên bảng dưới DB có 's'
@Getter
@Setter
public class Product { // Tên Class KHÔNG CÓ 's'

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "name")
    private String name; // Đã sửa thành String
    
    @Column(name = "product_code")
    private String productCode; // Đã sửa thành String
    
    @Column(name = "search_tag")
    private String searchTag; // Đã sửa thành String
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // Đã sửa thành String + TEXT
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "slug")
    private String slug; // Đã sửa thành String
    
    // --- CÁC MỐI QUAN HỆ MANY-TO-ONE (Trỏ ra ngoài) ---
    
    @ManyToOne
    @JoinColumn(name = "brand_id")
    private Brand brand; // Nên đổi tên class Brands thành Brand

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category; // Nên đổi tên class Categories thành Category

    // --- CÁC MỐI QUAN HỆ ONE-TO-MANY (Kéo thằng khác vào) ---

    // 1 Sản phẩm có nhiều Biến thể (Size, màu)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "products")
    private Set<ProductVariant> productVariants = new HashSet<>();

    // 1 Sản phẩm có nhiều Hình ảnh
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "products")
    private Set<ProductImage> productImages = new HashSet<>();

    // 1 Sản phẩm có nhiều Đánh giá
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "products")
    private Set<Review> reviews = new HashSet<>();
}