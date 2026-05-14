package com.javaweb.controller;

import com.javaweb.dto.*;
import com.javaweb.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import com.javaweb.dto.UserRequestDTO;

//Framework
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/users") // Cổng API cho Frontend gọi
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ==========================================
    // NHÓM API DÀNH CHO KHÁCH HÀNG (CUSTOMER) 
    // ==========================================

    @GetMapping("/my-profile")
    public ResponseEntity<UserDTO> getMyProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(userService.getMyProfile(email));
    }

    @PutMapping("/my-profile")
    public ResponseEntity<UserDTO> updateMyProfile(@RequestBody UserRequestDTO requestDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(userService.updateMyProfile(email, requestDTO));
    }

    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequestDTO requestDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        userService.changePassword(email, requestDTO);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/interests")
    public ResponseEntity<List<CategoryDTO>> getMyInterests() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(userService.getMyInterests(email));
    }

    @PutMapping("/interests")
    public ResponseEntity<Void> updateInterests(@RequestBody List<Long> categoryIds) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        userService.updateInterests(email, categoryIds);
        return ResponseEntity.ok().build();
    }

    // ==========================================
    // NHÓM API DÀNH CHO QUẢN TRỊ VIÊN (ADMIN)
    // ==========================================

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAll() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getById(@PathVariable Long id) {
        UserDTO userDTO = userService.getUserById(id);
        if (userDTO == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userDTO);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<UserDTO> create(@RequestBody UserRequestDTO requestDTO) {
        UserDTO newUser = userService.createUser(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> update(@PathVariable Long id, @RequestBody UserRequestDTO requestDTO) {
        UserDTO updatedUser = userService.updateUser(id, requestDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}