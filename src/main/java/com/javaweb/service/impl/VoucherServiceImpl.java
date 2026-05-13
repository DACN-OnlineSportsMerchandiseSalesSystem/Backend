package com.javaweb.service.impl;

import com.javaweb.dto.VoucherDTO;
import com.javaweb.dto.VoucherRequestDTO;
import com.javaweb.entity.Voucher;
import com.javaweb.exception.ResouceNotFoundException;
import com.javaweb.repository.VoucherRepository;
import com.javaweb.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.javaweb.repository.CategoryRepository;
import com.javaweb.repository.BrandRepository;
import com.javaweb.entity.Category;
import com.javaweb.entity.Brand;
import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    @Override
    public VoucherDTO createVoucher(VoucherRequestDTO requestDTO) {
        if (voucherRepository.findByCode(requestDTO.getCode()).isPresent()) {
            throw new RuntimeException("Voucher code already exists: " + requestDTO.getCode());
        }

        Voucher voucher = new Voucher();
        voucher.setCode(requestDTO.getCode());
        voucher.setDiscountAmount(requestDTO.getDiscountAmount());
        voucher.setMinOrderValue(requestDTO.getMinOrderValue());
        voucher.setUsageLimit(requestDTO.getUsageLimit());
        voucher.setExpiryDate(requestDTO.getExpiryDate());
        voucher.setUsedCount(0);

        if (requestDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(requestDTO.getCategoryId())
                    .orElseThrow(() -> new ResouceNotFoundException("Category not found with id: " + requestDTO.getCategoryId()));
            voucher.setCategory(category);
        }

        if (requestDTO.getBrandId() != null) {
            Brand brand = brandRepository.findById(requestDTO.getBrandId())
                    .orElseThrow(() -> new ResouceNotFoundException("Brand not found with id: " + requestDTO.getBrandId()));
            voucher.setBrand(brand);
        }



        Voucher savedVoucher = voucherRepository.save(voucher);
        return mapToDTO(savedVoucher);
    }

    @Override
    public VoucherDTO updateVoucher(Long id, VoucherRequestDTO requestDTO) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResouceNotFoundException("Voucher not found with id: " + id));

        // Nếu cập nhật mã code khác, kiểm tra trùng lặp
        if (!voucher.getCode().equals(requestDTO.getCode()) && 
            voucherRepository.findByCode(requestDTO.getCode()).isPresent()) {
            throw new RuntimeException("Voucher code already exists: " + requestDTO.getCode());
        }

        voucher.setCode(requestDTO.getCode());
        voucher.setDiscountAmount(requestDTO.getDiscountAmount());
        voucher.setMinOrderValue(requestDTO.getMinOrderValue());
        voucher.setUsageLimit(requestDTO.getUsageLimit());
        voucher.setExpiryDate(requestDTO.getExpiryDate());

        if (requestDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(requestDTO.getCategoryId())
                    .orElseThrow(() -> new ResouceNotFoundException("Category not found with id: " + requestDTO.getCategoryId()));
            voucher.setCategory(category);
        } else {
            voucher.setCategory(null);
        }

        if (requestDTO.getBrandId() != null) {
            Brand brand = brandRepository.findById(requestDTO.getBrandId())
                    .orElseThrow(() -> new ResouceNotFoundException("Brand not found with id: " + requestDTO.getBrandId()));
            voucher.setBrand(brand);
        } else {
            voucher.setBrand(null);
        }



        Voucher updatedVoucher = voucherRepository.save(voucher);
        return mapToDTO(updatedVoucher);
    }

    @Override
    public void deleteVoucher(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResouceNotFoundException("Voucher not found with id: " + id));
        voucherRepository.delete(voucher);
    }

    @Override
    public List<VoucherDTO> getAllVouchers() {
        return voucherRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<VoucherDTO> getValidVouchers() {
        return voucherRepository.findValidVouchers().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public VoucherDTO getVoucherById(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResouceNotFoundException("Voucher not found with id: " + id));
        return mapToDTO(voucher);
    }

    @Override
    public VoucherDTO checkVoucher(String code, BigDecimal orderValue) {
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new ResouceNotFoundException("Mã giảm giá không tồn tại: " + code));

        if (voucher.getExpiryDate() != null && voucher.getExpiryDate().before(new Date())) {
            throw new RuntimeException("Mã giảm giá đã hết hạn!");
        }

        if (voucher.getUsageLimit() != null && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng!");
        }

        if (voucher.getMinOrderValue() != null && orderValue.compareTo(voucher.getMinOrderValue()) < 0) {
            throw new RuntimeException("Đơn hàng chưa đạt giá trị tối thiểu " + voucher.getMinOrderValue() + " để áp dụng mã này!");
        }

        return mapToDTO(voucher);
    }

    private VoucherDTO mapToDTO(Voucher voucher) {
        VoucherDTO dto = new VoucherDTO();
        dto.setId(voucher.getId());
        dto.setCode(voucher.getCode());
        dto.setDiscountAmount(voucher.getDiscountAmount());
        dto.setMinOrderValue(voucher.getMinOrderValue());
        dto.setUsageLimit(voucher.getUsageLimit());
        dto.setUsedCount(voucher.getUsedCount());
        dto.setExpiryDate(voucher.getExpiryDate());
        dto.setCreatedAt(voucher.getCreatedAt());
        if (voucher.getCategory() != null) {
            dto.setCategoryId(voucher.getCategory().getId());
            dto.setCategoryName(voucher.getCategory().getName());
        }
        if (voucher.getBrand() != null) {
            dto.setBrandId(voucher.getBrand().getId());
            dto.setBrandName(voucher.getBrand().getName());
        }

        return dto;
    }
}
