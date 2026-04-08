package com.javaweb.service.impl;

import com.javaweb.dto.UserDTO;
import com.javaweb.entity.User;
import com.javaweb.repository.UserRepository;
import com.javaweb.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.javaweb.exception.*;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    
    // Inject thêm RoleRepository để gán Quyền lúc tạo User
    private final com.javaweb.repository.RoleRepository roleRepository;

    @Override
    public List<UserDTO> getAllUsers() {
        // Lấy tất cả User từ MySQL
        List<User> users = userRepository.findAll();
        
        // Tạo một cái giỏ rỗng để chứa DTO
        List<UserDTO> userDTOs = new ArrayList<>();
        
        // Đổ dữ liệu từ Entity sang DTO
        for (User user : users) {
            UserDTO dto = new UserDTO();
            dto.setId(user.getId());
            dto.setFullName(user.getFullName());
            dto.setEmail(user.getEmail());
            dto.setPhone(user.getPhone());
            dto.setStatus(user.getStatus());
            
            // Xử lý cẩn thận cái Role (Kiểm tra null để tránh lỗi NullPointerException)
            if (user.getRole() != null) {
                dto.setRoleName(user.getRole().getName());
            }
            
            userDTOs.add(dto);
        }
        
        return userDTOs;
    }

    @Override
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id).orElse(null);
        
        if (user == null) {
            throw new ResouceNotFoundException("User not found with id: " + id); // Tốt nhất sau này nên ném ra một Custom Exception như UserNotFoundException
        }

        // Chuyển Entity sang DTO
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus());
        
        if (user.getRole() != null) {
            dto.setRoleName(user.getRole().getName());
        }
        
        return dto;
    }

    @Override
    public UserDTO createUser(com.javaweb.dto.UserRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("email"); 
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new BadRequestException("phone"); 
        }
        if (request.getPassword().length() < 6) {
            throw new BadRequestException("password"); 
        }
        
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(request.getPassword());
        user.setStatus("ACTIVE");
        
        if (request.getRoleName() != null) {
            com.javaweb.entity.Role role = roleRepository.findByName(request.getRoleName()).orElse(null);
            user.setRole(role);
        }

        user = userRepository.save(user); 

        
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus());
        if (user.getRole() != null) {
            dto.setRoleName(user.getRole().getName());
        }
        return dto;
    }

    @Override
    public UserDTO updateUser(Long id, com.javaweb.dto.UserRequestDTO request) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new ResouceNotFoundException("User not found with id: " + id);
        }
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(request.getPassword());
        user.setStatus(request.getStatus());
        if (request.getRoleName() != null) {
            com.javaweb.entity.Role role = roleRepository.findByName(request.getRoleName()).orElse(null);
            user.setRole(role);
        }
        user = userRepository.save(user);
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus());
        if (user.getRole() != null) {
            dto.setRoleName(user.getRole().getName());
        }
        return dto;
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new ResouceNotFoundException("User not found with id: " + id);
        }

        //không thể xoá thằng dữ liệu bằng cách deletebyid được
        user.setStatus("INACTIVE");
        userRepository.save(user);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }

    
}