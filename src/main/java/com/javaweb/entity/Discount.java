package com.javaweb.entity;

import com.javaweb.enums.DiscountScope;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Table(name = "discounts")
@Getter
@Setter
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name; // VD: "Flash Sale Nike", "Giảm giá mùa hè"

    @Column(name = "discount_percent", nullable = false)
    private Integer discountPercent; // 0–100

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false)
    private DiscountScope scope; // GLOBAL, CATEGORY, BRAND

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category; // Nullable: chỉ có khi scope = CATEGORY

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand; // Nullable: chỉ có khi scope = BRAND

    @Column(name = "start_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate; // Null = bắt đầu ngay

    @Column(name = "end_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate; // Null = không hết hạn

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    @CreationTimestamp
    private Date createdAt;
}
