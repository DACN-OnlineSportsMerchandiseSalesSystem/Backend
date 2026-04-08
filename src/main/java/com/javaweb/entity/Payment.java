package com.javaweb.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;
//import org.hibernate.annotations.UpdateTimestamp;




@Entity
@Table(name = "payment") // Tên bảng dưới DB có 's'
@Getter
@Setter


public class Payment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	@Column(name = "payment_method")
	private String paymentMethod;
	
	@Column(name = "amount")
	private Long amount;
	
	@Column(name = "transaction_code")
	private String transactionCode;
	
	@Column(name ="created_at")
	@CreationTimestamp
	private Date createAt;
	
	@Column(name = "qr_code")
	private String qrCode;
	
	@ManyToOne
	@JoinColumn(name = "order_id")
	private Orders orders;
	
}
