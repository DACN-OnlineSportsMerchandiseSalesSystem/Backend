package com.javaweb.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

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
	
	@Column(name = "discount")
	private Integer discount;
	
	@Column(name = "original_price")
	private BigDecimal originalPrice;
	
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
	
	public BigDecimal getPrice() {
		if (this.price == null && this.originalPrice != null) {
			int disc = this.discount != null ? this.discount : 0;
			return this.originalPrice.multiply(BigDecimal.valueOf(100 - disc)).divide(BigDecimal.valueOf(100));
		}
		return this.price;
	}

	public BigDecimal getPrice(List<Discount> activeDiscounts) {
		if (this.originalPrice == null) return this.price;
		
		int manualDiscount = this.discount != null ? this.discount : 0;
		if (activeDiscounts == null || activeDiscounts.isEmpty()) {
			return this.originalPrice.multiply(BigDecimal.valueOf(100 - manualDiscount)).divide(BigDecimal.valueOf(100));
		}
		
		int promoDiscount = activeDiscounts.stream().filter(d -> {
			if (d.getScope() == com.javaweb.enums.DiscountScope.GLOBAL) return true;
			if (d.getScope() == com.javaweb.enums.DiscountScope.BRAND && this.products != null && this.products.getBrand() != null && this.products.getBrand().getId().equals(d.getBrand() != null ? d.getBrand().getId() : null)) return true;
			if (d.getScope() == com.javaweb.enums.DiscountScope.CATEGORY && d.getCategory() != null && this.products != null && this.products.getCategories() != null)
				return this.products.getCategories().stream().anyMatch(c -> c.getId().equals(d.getCategory().getId()));
			return false;
		}).mapToInt(Discount::getDiscountPercent).max().orElse(0);
		
		int finalDiscount = Math.max(manualDiscount, promoDiscount);
		return this.originalPrice.multiply(BigDecimal.valueOf(100 - finalDiscount)).divide(BigDecimal.valueOf(100));
	}
}
