package com.javaweb.repository;

import com.javaweb.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List; 

public interface OrderRepository extends JpaRepository<Orders, Long>{
	List<Orders> findByNameContaining(String name);
}
