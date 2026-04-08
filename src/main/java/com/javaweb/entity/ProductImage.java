package com.javaweb.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "product_image")
@Getter
@Setter

public class ProductImage {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	@Column(name = "image_url")
	private String imageUrl;
	
	@Column(name = "is_thumbnail")
	private Boolean isThumbnail;
	
	@Column(name = "display_order")
	private String displayOrder;
	
	@ManyToOne
	@JoinColumn(name = "product_id")
	private Product products;
	
	
}
