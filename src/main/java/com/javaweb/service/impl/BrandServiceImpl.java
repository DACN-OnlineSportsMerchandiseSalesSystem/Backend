package com.javaweb.service.impl;

import com.javaweb.dto.BrandDTO;
import com.javaweb.entity.Brand;
import com.javaweb.repository.BrandRepository;
import com.javaweb.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;

    @Override
    public List<BrandDTO> getAllBrands() {
        List<Brand> brands = brandRepository.findAll();
        List<BrandDTO> brandDTOs = new ArrayList<>();

        for (Brand brand : brands) {
            BrandDTO dto = new BrandDTO();
            dto.setId(brand.getId());
            dto.setName(brand.getName());
            dto.setDetail(brand.getDetail());
            dto.setImageUrl(brand.getImageUrl());
            dto.setStatus(brand.getStatus());
            dto.setRating(brand.getRating());
            brandDTOs.add(dto);
        }
        return brandDTOs;
    }

    @Override
    public BrandDTO createBrand(BrandDTO brandDTO) {
        Brand brand = new Brand();
        brand.setName(brandDTO.getName());
        brand.setDetail(brandDTO.getDetail());
        brand.setImageUrl(brandDTO.getImageUrl());
        brand.setStatus(brandDTO.getStatus());
        brand.setRating(brandDTO.getRating());
        brand = brandRepository.save(brand);
        brandDTO.setId(brand.getId());
        return brandDTO;
    }

    @Override
    public BrandDTO updateBrand(Long id, BrandDTO brandDTO) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found with id: " + id));
        brand.setName(brandDTO.getName());
        brand.setDetail(brandDTO.getDetail());
        brand.setImageUrl(brandDTO.getImageUrl());
        brand.setStatus(brandDTO.getStatus());
        brand.setRating(brandDTO.getRating());
        brandRepository.save(brand);
        brandDTO.setId(brand.getId());
        return brandDTO;
    }

    @Override
    public void deleteBrand(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found with id: " + id));
        brandRepository.delete(brand);
    }
}
