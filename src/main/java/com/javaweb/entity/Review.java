package com.javaweb.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content; // Nội dung đánh giá

    @Column(name = "rating")
    private Integer rating; // Điểm sao (1-5)

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product products; // Khớp với mappedBy="products"

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // Khớp với mappedBy="user" trong User.java
}
