package com.javaweb.service.impl;

import com.javaweb.dto.DiscountDTO;
import com.javaweb.dto.DiscountRequestDTO;
import com.javaweb.entity.Brand;
import com.javaweb.entity.Category;
import com.javaweb.entity.Discount;
import com.javaweb.enums.DiscountScope;
import com.javaweb.exception.ResouceNotFoundException;
import com.javaweb.repository.BrandRepository;
import com.javaweb.repository.CategoryRepository;
import com.javaweb.repository.DiscountRepository;
import com.javaweb.service.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    @Override
    public List<DiscountDTO> getAllDiscounts() {
        return discountRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<DiscountDTO> getActiveDiscounts() {
        return discountRepository.findAllActiveDiscounts(new Date()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DiscountDTO getDiscountById(Long id) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new ResouceNotFoundException("Không tìm thấy discount với id: " + id));
        return mapToDTO(discount);
    }

    @Override
    public DiscountDTO createDiscount(DiscountRequestDTO request) {
        Discount discount = new Discount();
        return mapToDTO(discountRepository.save(applyRequest(discount, request)));
    }

    @Override
    public DiscountDTO updateDiscount(Long id, DiscountRequestDTO request) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new ResouceNotFoundException("Không tìm thấy discount với id: " + id));
        return mapToDTO(discountRepository.save(applyRequest(discount, request)));
    }

    @Override
    public void deleteDiscount(Long id) {
        if (!discountRepository.existsById(id)) {
            throw new ResouceNotFoundException("Không tìm thấy discount với id: " + id);
        }
        discountRepository.deleteById(id);
    }

    // --- HELPER ---

    private Discount applyRequest(Discount discount, DiscountRequestDTO request) {
        discount.setName(request.getName());
        discount.setDiscountPercent(request.getDiscountPercent());
        discount.setScope(request.getScope());
        discount.setStartDate(request.getStartDate());
        discount.setEndDate(request.getEndDate());
        discount.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        // Reset liên kết cũ
        discount.setCategory(null);
        discount.setBrand(null);

        if (request.getScope() == DiscountScope.CATEGORY && request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResouceNotFoundException("Category not found: " + request.getCategoryId()));
            discount.setCategory(category);
        } else if (request.getScope() == DiscountScope.BRAND && request.getBrandId() != null) {
            Brand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new ResouceNotFoundException("Brand not found: " + request.getBrandId()));
            discount.setBrand(brand);
        }

        return discount;
    }

    private DiscountDTO mapToDTO(Discount d) {
        DiscountDTO dto = new DiscountDTO();
        dto.setId(d.getId());
        dto.setName(d.getName());
        dto.setDiscountPercent(d.getDiscountPercent());
        dto.setScope(d.getScope());
        dto.setStartDate(d.getStartDate());
        dto.setEndDate(d.getEndDate());
        dto.setIsActive(d.getIsActive());
        dto.setCreatedAt(d.getCreatedAt());
        if (d.getCategory() != null) {
            dto.setCategoryId(d.getCategory().getId());
            dto.setCategoryName(d.getCategory().getName());
        }
        if (d.getBrand() != null) {
            dto.setBrandId(d.getBrand().getId());
            dto.setBrandName(d.getBrand().getName());
        }
        return dto;
    }
}
