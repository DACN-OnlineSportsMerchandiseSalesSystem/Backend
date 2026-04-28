package com.javaweb.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
public class OrderItems {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name="price_at_purchase")
    private BigDecimal priceAtPurchase;

    @Column(name="discount_amount")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name="quantity")
    private int quantity;



    @ManyToOne
    @JoinColumn(name = "order_id")
    private Orders orders;
    
    @ManyToOne
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariants;
    
}
