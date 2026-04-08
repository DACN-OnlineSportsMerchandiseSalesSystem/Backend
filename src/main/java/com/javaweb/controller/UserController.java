package com.javaweb.controller;

import com.javaweb.dto.UserDTO;
import com.javaweb.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users") // Cổng API cho Frontend gọi
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // HTTP GET: localhost:8080/api/users
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAll() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // HTTP GET: localhost:8080/api/users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getById(@org.springframework.web.bind.annotation.PathVariable Long id) {
        UserDTO userDTO = userService.getUserById(id);
        if (userDTO == null) {
            return ResponseEntity.notFound().build(); // Trả về lỗi 404 Not Found nếu không có
        }
        return ResponseEntity.ok(userDTO);
    }

    // HTTP POST: localhost:8080/api/users
    @org.springframework.web.bind.annotation.PostMapping
    public ResponseEntity<UserDTO> create(@org.springframework.web.bind.annotation.RequestBody com.javaweb.dto.UserRequestDTO requestDTO) {
        UserDTO newUser = userService.createUser(requestDTO);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(newUser);
    }

    // HTTP PUT: localhost:8080/api/users/{id}
    @org.springframework.web.bind.annotation.PutMapping("/{id}")
    public ResponseEntity<UserDTO> update(@org.springframework.web.bind.annotation.PathVariable Long id, @org.springframework.web.bind.annotation.RequestBody com.javaweb.dto.UserRequestDTO requestDTO) {
        UserDTO updatedUser = userService.updateUser(id, requestDTO);
        return ResponseEntity.ok(updatedUser);
    }

    // HTTP DELETE: localhost:8080/api/users/{id}
    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@org.springframework.web.bind.annotation.PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build(); // Trả về mã 204 No Content báo hiệu xóa (hoặc ẩn) thành công
    }
}