package com.javaweb.service;

import com.javaweb.dto.UserDTO;
import java.util.List;

public interface UserService{
    List<UserDTO> getAllUsers();
    UserDTO getUserById(Long id);
    UserDTO createUser(com.javaweb.dto.UserRequestDTO request);
    UserDTO updateUser(Long id, com.javaweb.dto.UserRequestDTO request);
    void deleteUser(Long id);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);    
}