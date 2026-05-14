package com.javaweb.service.impl;

import com.javaweb.dto.*;
import com.javaweb.entity.*;
import com.javaweb.repository.UserRepository;
import com.javaweb.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.javaweb.exception.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.javaweb.repository.RoleRepository;
import com.javaweb.enums.UserStatus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import com.javaweb.repository.CategoryRepository;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    // Inject thêm RoleRepository để gán Quyền lúc tạo User
    private final RoleRepository roleRepository;
    // Inject cỗ máy băm mật khẩu
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;

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
            dto.setFirstName(user.getFirstName());
            dto.setLastName(user.getLastName());
            dto.setEmail(user.getEmail());
            dto.setPhone(user.getPhone());
            dto.setStatus(user.getStatus());
            dto.setLevel(user.getLevel());
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
            throw new ResouceNotFoundException("User not found with id: " + id); // Tốt nhất sau này nên ném ra một
                                                                                 // Custom Exception như
                                                                                 // UserNotFoundException
        }

        // Chuyển Entity sang DTO
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus());
        dto.setLevel(user.getLevel());
        if (user.getRole() != null) {
            dto.setRoleName(user.getRole().getName());
        }

        return dto;
    }

    @Override
    public UserDTO createUser(UserRequestDTO request) {
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
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        // Cực kì quan trọng: Băm mật khẩu ra thành chuỗi mã hóa trước khi cho vào DB
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatus.ACTIVE);

        if (request.getRoleName() != null) {
            Role role = roleRepository.findByName(request.getRoleName()).orElseGet(() -> {
                Role newRole = new com.javaweb.entity.Role();
                newRole.setName(request.getRoleName());
                return roleRepository.save(newRole);
            });
            user.setRole(role);
        }

        user = userRepository.save(user);

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus());
        dto.setLevel(user.getLevel());
        if (user.getRole() != null) {
            dto.setRoleName(user.getRole().getName());
        }
        return dto;
    }

    @Override
    public UserDTO updateUser(Long id, UserRequestDTO request) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new ResouceNotFoundException("User not found with id: " + id);
        }
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        user.setStatus(request.getStatus());
        if (request.getRoleName() != null) {
            com.javaweb.entity.Role role = roleRepository.findByName(request.getRoleName()).orElseGet(() -> {
                com.javaweb.entity.Role newRole = new com.javaweb.entity.Role();
                newRole.setName(request.getRoleName());
                return roleRepository.save(newRole);
            });
            user.setRole(role);
        }
        user = userRepository.save(user);
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus());
        dto.setLevel(user.getLevel());
        if (user.getRole() != null) {
            dto.setRoleName(user.getRole().getName());
        }
        return dto;
    }

    @Override
    public UserDTO getMyProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResouceNotFoundException("User not found: " + email));
                
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus());
        dto.setLevel(user.getLevel());
        if (user.getRole() != null) {
            dto.setRoleName(user.getRole().getName());
        }
        return dto;
    }

    @Override
    public UserDTO updateMyProfile(String email, UserRequestDTO request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResouceNotFoundException("User not found: " + email));

        // Chỉ cho phép cập nhật thông tin cơ bản, chặn thay đổi Status và Role
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());

        user = userRepository.save(user);

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus());
        dto.setLevel(user.getLevel());
        if (user.getRole() != null) {
            dto.setRoleName(user.getRole().getName());
        }
        return dto;
    }

    @Override
    public void changePassword(String email, ChangePasswordRequestDTO request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResouceNotFoundException("User not found: " + email));

        // Kiểm tra mật khẩu cũ
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu hiện tại không chính xác!");
        }

        // Mã hóa và lưu mật khẩu mới
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new ResouceNotFoundException("User not found with id: " + id);
        }

        // không thể xoá thằng dữ liệu bằng cách deletebyid được
        user.setStatus(UserStatus.INACTIVE);
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

    @Override
    public void updateInterests(String email, List<Long> categoryIds) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResouceNotFoundException("User not found: " + email));

        if (categoryIds == null || categoryIds.isEmpty()) {
            user.setInterestedCategories(new HashSet<>());
        } else {
            List<Category> categories = categoryRepository.findAllById(categoryIds);
            user.setInterestedCategories(new HashSet<>(categories));
        }

        userRepository.save(user);
    }

    @Override
    public List<CategoryDTO> getMyInterests(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResouceNotFoundException("User not found: " + email));

        return user.getInterestedCategories().stream()
                .map(cat -> {
                    CategoryDTO dto = new CategoryDTO();
                    dto.setId(cat.getId());
                    dto.setName(cat.getName());
                    dto.setSlug(cat.getSlug());
                    dto.setStatus(cat.getStatus());
                    dto.setRating(cat.getRating());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}