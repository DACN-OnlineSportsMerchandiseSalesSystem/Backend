package com.javaweb.service;

import com.javaweb.dto.UserDTO;
import com.javaweb.dto.UserRequestDTO;
import java.util.List;

public interface UserService {
    List<UserDTO> getAllUsers();

    UserDTO getUserById(Long id);

    UserDTO createUser(UserRequestDTO request);

    UserDTO updateUser(Long id, UserRequestDTO request);

    UserDTO getMyProfile(String email);

    UserDTO updateMyProfile(String email, UserRequestDTO request);

    void changePassword(String email, com.javaweb.dto.ChangePasswordRequestDTO request);

    void deleteUser(Long id);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);
}