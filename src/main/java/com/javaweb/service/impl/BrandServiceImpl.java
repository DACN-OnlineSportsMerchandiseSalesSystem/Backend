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
            dto.setImage(brand.getImage());
            dto.setUrl(brand.getUrl());
            brandDTOs.add(dto);
        }
        return brandDTOs;
    }
}
