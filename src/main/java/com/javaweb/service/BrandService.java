package com.javaweb.service;

import com.javaweb.dto.BrandDTO;
import java.util.List;

public interface BrandService {
    List<BrandDTO> getAllBrands();
    BrandDTO createBrand(BrandDTO brandDTO);
    BrandDTO updateBrand(Long id, BrandDTO brandDTO);
    void deleteBrand(Long id);
}
