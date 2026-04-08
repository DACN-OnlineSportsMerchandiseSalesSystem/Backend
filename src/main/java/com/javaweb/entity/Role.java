package com.javaweb.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
public class Role {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id; // Dùng Integer cho Role là cực kỳ hợp lý
	
    @Column(name ="name")
    private String name;

    // 1 Quyền (Role) được cấp cho rất nhiều Người dùng (Users)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "role") // Đã đổi thành "role"
    private Set<User> users = new HashSet<>(); // Đã thêm "s"
	
}