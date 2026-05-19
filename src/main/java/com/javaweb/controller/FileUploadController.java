package com.javaweb.controller;

import com.javaweb.service.CloudinaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "File Uploads", description = "Endpoints for uploading images and product media files to Cloudinary hosting")
public class FileUploadController {

    private final CloudinaryService cloudinaryService;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority('ADMIN')") // Thường thì Admin mới được quyền upload ảnh sản phẩm
    @Operation(summary = "Upload image to Cloudinary", description = "Admin only. Uploads a product picture or blog header media file directly to Cloudinary storage and returns the secure URL.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Image uploaded successfully and secure URL returned"),
        @ApiResponse(responseCode = "400", description = "No file chosen or selected file is empty"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN authority"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error - Cloudinary upload stream failed")
    })
    public ResponseEntity<Map<String, String>> uploadImage(
            @Parameter(description = "Multipart file object of the image", required = true)
            @RequestParam("file") MultipartFile file) {
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
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi tải ảnh: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
