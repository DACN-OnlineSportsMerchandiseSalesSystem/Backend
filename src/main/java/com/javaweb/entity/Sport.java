package com.javaweb.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "sports")
@Getter
@Setter
public class Sport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "slug")
    private String slug;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "status")
    private String status;

    @Column(name = "discount")
    private Integer discount;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "sport")
    private Set<Product> products = new HashSet<>();
}
