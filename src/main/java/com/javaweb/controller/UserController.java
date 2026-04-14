package com.javaweb.controller;

import com.javaweb.dto.UserDTO;
import com.javaweb.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import com.javaweb.dto.UserRequestDTO;

//Framework
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/users") // Cổng API cho Frontend gọi
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Tất cả API trong kho này đều yêu cầu quyền ADMIN
public class UserController {

    private final UserService userService;

    // HTTP GET: localhost:8080/api/users
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAll() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // HTTP GET: localhost:8080/api/users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getById(@PathVariable Long id) {
        UserDTO userDTO = userService.getUserById(id);
        if (userDTO == null) {
            return ResponseEntity.notFound().build(); // Trả về lỗi 404 Not Found nếu không có
        }
        return ResponseEntity.ok(userDTO);
    }

    // HTTP POST: localhost:8080/api/users
    @PostMapping
    public ResponseEntity<UserDTO> create(
            @RequestBody UserRequestDTO requestDTO) {
        UserDTO newUser = userService.createUser(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    // HTTP PUT: localhost:8080/api/users/{id}
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> update(@PathVariable Long id,
            @RequestBody UserRequestDTO requestDTO) {
        UserDTO updatedUser = userService.updateUser(id, requestDTO);
        return ResponseEntity.ok(updatedUser);
    }

    // HTTP DELETE: localhost:8080/api/users/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build(); // Trả về mã 204 No Content báo hiệu xóa (hoặc ẩn) thành công
    }
}