package com.javaweb.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadFile(MultipartFile file) throws IOException {
        try {
            // Upload ảnh lên Cloudinary với tham số tự động nhận diện loại file
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("resource_type", "auto"));
            
            // Lấy secure_url (đường dẫn https) trả về
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new IOException("Lỗi khi tải ảnh lên Cloudinary: " + e.getMessage());
        }
    }
}
