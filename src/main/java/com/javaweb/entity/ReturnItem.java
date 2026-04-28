package com.javaweb.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "return_items")
@Getter
@Setter
public class ReturnItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "image_proof")
    private String imageProof;

    // --- CÁC MỐI QUAN HỆ ---

    @ManyToOne
    @JoinColumn(name = "return_request_id")
    private ReturnRequest returnRequest;

    @ManyToOne
    @JoinColumn(name = "order_item_id")
    private OrderItems orderItem;
}
