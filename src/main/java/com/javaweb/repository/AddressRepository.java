package com.javaweb.repository;

import com.javaweb.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;


public interface AddressRepository extends JpaRepository<Address, Long> {

}
