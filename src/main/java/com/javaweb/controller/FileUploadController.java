package com.javaweb.controller;

import com.javaweb.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FileUploadController {

    private final CloudinaryService cloudinaryService;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN')") // ADMIN mới được quyền upload ảnh
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Vui lòng chọn file ảnh để tải lên!");
                return ResponseEntity.badRequest().body(error);
            }

            // Gọi service upload
            String imageUrl = cloudinaryService.uploadFile(file);

            // Trả về JSON chứa URL
            Map<String, String> response = new HashMap<>();
            response.put("url", imageUrl);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace(); // In lỗi chi tiết ra Console của Backend
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi tải ảnh: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
