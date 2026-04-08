package com.javaweb.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "product_variants")
@Getter
@Setter

public class ProductVariant {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

	@Column(name = "sku_code")
	private String skuCode;
	
	@Column(name = "gender")
	private String gender;
	
	@Column(name = "size")
	private String size;
	
	@Column(name = "price")
	private BigDecimal price;
	
	@Column(name = "color")
	private String color;
	
	@Column(name = "stock_quantity")
	private Integer stockQuantity;
	
	@Column(name = "weight")
	private Integer weight;
	
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product products;
	
    
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "productVariants")
	private Set<OrderItems> orderItems = new HashSet<>();
	
	public void add(OrderItems item) {
		if(item != null) {
			if(this.orderItems == null) {
				this.orderItems= new HashSet<>();
			}
			this.orderItems.add(item);
			item.setProductVariants(this);
		}
	}
	

}
