package com.javaweb.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "address")
@Getter
@Setter

public class Address {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "street")
    private String street;

    @Column(name = "city")
    private String city; 	

    @Column(name = "ward")
    private String state;

    @Column(name = "is_default")
    private Boolean isDefault;

    @Column(name = "receiver_name")
    private String receiverName;
    
    @Column(name = "phone")
    private String phone;

}
