package com.javaweb.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "cart_items") // Đã thêm 's' cho khớp với Database
@Getter
@Setter

public class CartItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	@Column(name = "created_at")
	@CreationTimestamp
	private Date createdAt;
	
	@Column(name = "quantity")
	private BigDecimal quantity;
	
	@ManyToOne
	@JoinColumn(name = "cart_id")
	private Cart cart;
	
}
