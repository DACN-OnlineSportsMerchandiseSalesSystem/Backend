package com.javaweb.service;

import com.javaweb.dto.UserDTO;
import com.javaweb.dto.UserRequestDTO;
import com.javaweb.enums.UserStatus;
import java.util.List;

public interface UserService {
    List<UserDTO> getAllUsers();

    List<UserDTO> getUsers(String search, UserStatus status, String roleName);

    UserDTO getUserById(Long id);

    UserDTO createUser(UserRequestDTO request);

    UserDTO updateUser(Long id, UserRequestDTO request);

    UserDTO updateUserStatus(Long id, UserStatus status);

    UserDTO updateUserRole(Long id, String roleName);

    UserDTO getMyProfile(String email);

    UserDTO updateMyProfile(String email, UserRequestDTO request);

    void changePassword(String email, com.javaweb.dto.ChangePasswordRequestDTO request);

    void deleteUser(Long id);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    void updateInterests(String email, List<Long> categoryIds);

    List<com.javaweb.dto.CategoryDTO> getMyInterests(String email);
}
