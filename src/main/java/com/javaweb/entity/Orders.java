package com.javaweb.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
//import org.hibernate.annotations.UpdateTimestamp;


import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "orders")
@Getter
@Setter

public class Orders {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "create_at")
    @CreationTimestamp
    private Date createAt;

    @Column(name = "note")
    private String note; 	

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(name = "shipping_fee")
    private Long shippingFee;

    @Column(name = "receiver_name")
    private String receiverName;
    
    @Column(name = "phone")
    private String phone;
    
    @Column(name = "status")
    private String status;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "orders")
    private Set<OrderItems> orderItems = new HashSet<>();
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "billing_address_id", referencedColumnName = "id")
    private Address billingAddress;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    public void add(OrderItems item) {

        if (item != null) {
            if (this.orderItems == null) {
                this.orderItems = new HashSet<>();
            }

            this.orderItems.add(item);
            item.setOrders(this);
        }
    }

}
