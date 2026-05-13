package com.javaweb.service;

import com.javaweb.dto.DiscountDTO;
import com.javaweb.dto.DiscountRequestDTO;
import java.util.List;

public interface DiscountService {
    List<DiscountDTO> getAllDiscounts();
    List<DiscountDTO> getActiveDiscounts();
    DiscountDTO getDiscountById(Long id);
    DiscountDTO createDiscount(DiscountRequestDTO request);
    DiscountDTO updateDiscount(Long id, DiscountRequestDTO request);
    void deleteDiscount(Long id);
}
