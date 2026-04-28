package com.javaweb.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String title; // Tiêu đề đánh giá

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment; // Nội dung đánh giá

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt; // Thời gian đánh giá

    @Column(name = "admin_reply", columnDefinition = "TEXT")
    private String adminReply; // Lời phản hồi của Admin

    @Column(name = "replied_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date repliedAt; // Thời gian Admin phản hồi

    @Column(name = "rating")
    private Integer rating; // Điểm sao (1-5)

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product products; // Khớp với mappedBy="products"

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // Khớp với mappedBy="user" trong User.java
}
