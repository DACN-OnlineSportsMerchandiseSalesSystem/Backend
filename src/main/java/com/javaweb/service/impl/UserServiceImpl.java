package com.javaweb.service.impl;

import com.javaweb.dto.CategoryDTO;
import com.javaweb.dto.ChangePasswordRequestDTO;
import com.javaweb.dto.UserDTO;
import com.javaweb.dto.UserRequestDTO;
import com.javaweb.entity.Category;
import com.javaweb.entity.Role;
import com.javaweb.entity.User;
import com.javaweb.enums.UserStatus;
import com.javaweb.exception.BadRequestException;
import com.javaweb.exception.ResouceNotFoundException;
import com.javaweb.repository.CategoryRepository;
import com.javaweb.repository.RoleRepository;
import com.javaweb.repository.UserRepository;
import com.javaweb.security.AppRoles;
import com.javaweb.service.UserService;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;

    @Override
    public List<UserDTO> getAllUsers() {
        return getUsers(null, null, null);
    }

    @Override
    public List<UserDTO> getUsers(String search, UserStatus status, String roleName) {
        Specification<User> spec = Specification.where(null);

        if (search != null && !search.trim().isEmpty()) {
            String keyword = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("email")), keyword),
                    cb.like(cb.lower(root.get("phone")), keyword),
                    cb.like(cb.lower(root.get("firstName")), keyword),
                    cb.like(cb.lower(root.get("lastName")), keyword)
            ));
        }

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        if (roleName != null && !roleName.trim().isEmpty()) {
            String normalizedRole = roleName.trim().toUpperCase(Locale.ROOT);
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.upper(root.join("role", JoinType.LEFT).get("name")), normalizedRole));
        }

        return userRepository.findAll(spec).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResouceNotFoundException("User not found with id: " + id));
        return toDTO(user);
    }

    @Override
    public UserDTO createUser(UserRequestDTO request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("email is required");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("email");
        }
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw new BadRequestException("phone");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new BadRequestException("password");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setGender(request.getGender());
        user.setBirthDate(request.getBirthDate());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatus.ACTIVE);

        if (request.getRoleName() != null) {
            user.setRole(resolveRole(request.getRoleName()));
        }

        return toDTO(userRepository.save(user));
    }

    @Override
    public UserDTO updateUser(Long id, UserRequestDTO request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResouceNotFoundException("User not found with id: " + id));

        if (request.getEmail() != null && userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new BadRequestException("email");
        }
        if (request.getPhone() != null && userRepository.existsByPhoneAndIdNot(request.getPhone(), id)) {
            throw new BadRequestException("phone");
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getBirthDate() != null) {
            user.setBirthDate(request.getBirthDate());
        }
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            if (request.getPassword().length() < 6) {
                throw new BadRequestException("password");
            }
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        if (request.getRoleName() != null) {
            user.setRole(resolveRole(request.getRoleName()));
        }

        return toDTO(userRepository.save(user));
    }

    @Override
    public UserDTO updateUserStatus(Long id, UserStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("status is required");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResouceNotFoundException("User not found with id: " + id));
        user.setStatus(status);
        return toDTO(userRepository.save(user));
    }

    @Override
    public UserDTO updateUserRole(Long id, String roleName) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResouceNotFoundException("User not found with id: " + id));
        user.setRole(resolveRole(roleName));
        return toDTO(userRepository.save(user));
    }

    @Override
    public UserDTO getMyProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResouceNotFoundException("User not found: " + email));
        return toDTO(user);
    }

    @Override
    public UserDTO updateMyProfile(String email, UserRequestDTO request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResouceNotFoundException("User not found: " + email));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            if (userRepository.existsByPhoneAndIdNot(request.getPhone(), user.getId())) {
                throw new BadRequestException("phone");
            }
            user.setPhone(request.getPhone());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getBirthDate() != null) {
            user.setBirthDate(request.getBirthDate());
        }

        return toDTO(userRepository.save(user));
    }

    @Override
    public void changePassword(String email, ChangePasswordRequestDTO request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResouceNotFoundException("User not found: " + email));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            throw new BadRequestException("password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResouceNotFoundException("User not found with id: " + id));
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
    @org.springframework.transaction.annotation.Transactional
    public void updateInterests(String email, List<Long> categoryIds) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResouceNotFoundException("User not found: " + email));

        user.getInterestedCategories().clear();
        if (categoryIds != null && !categoryIds.isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(categoryIds);
            user.getInterestedCategories().addAll(categories);
        }

        userRepository.save(user);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
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

    private Role resolveRole(String roleName) {
        if (roleName == null || roleName.trim().isEmpty()) {
            throw new IllegalArgumentException("roleName is required");
        }
        String normalizedRole = roleName.trim().toUpperCase(Locale.ROOT);
        if (!AppRoles.isAllowed(normalizedRole)) {
            throw new IllegalArgumentException("Unsupported roleName. Allowed values: CUSTOMER, ADMIN, IT_ADMIN");
        }
        return roleRepository.findByNameIgnoreCase(normalizedRole).orElseGet(() -> {
            Role newRole = new Role();
            newRole.setName(normalizedRole);
            return roleRepository.save(newRole);
        });
    }

    private UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus());
        dto.setGender(user.getGender());
        dto.setBirthDate(user.getBirthDate());
        dto.setLevel(user.getLevel());
        dto.setRank(user.getRank());
        if (user.getRole() != null) {
            dto.setRoleName(user.getRole().getName());
        }
        return dto;
    }
}
