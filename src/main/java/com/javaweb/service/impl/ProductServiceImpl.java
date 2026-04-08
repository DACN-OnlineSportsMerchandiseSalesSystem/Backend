package com.javaweb.service.impl;

import com.javaweb.dto.ProductDTO;
import com.javaweb.dto.ProductRequestDTO;
import com.javaweb.entity.Product;
import com.javaweb.entity.Category;
import com.javaweb.entity.Brand;
import com.javaweb.repository.ProductRepository;
import com.javaweb.repository.CategoryRepository;
import com.javaweb.repository.BrandRepository;
import com.javaweb.service.ProductService;
import com.javaweb.exception.ResouceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    @Override
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResouceNotFoundException("Product not found with id: " + id));
        return mapToDTO(product);
    }

    @Override
    public ProductDTO createProduct(ProductRequestDTO request) {
        Product product = new Product();
        return mapToDTO(productRepository.save(mapToEntity(product, request)));
    }

    @Override
    public ProductDTO updateProduct(Long id, ProductRequestDTO request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResouceNotFoundException("Product not found with id: " + id));
        return mapToDTO(productRepository.save(mapToEntity(product, request)));
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResouceNotFoundException("Product not found with id: " + id));
        product.setStatus("INACTIVE"); 
        productRepository.save(product);
    }

    private Product mapToEntity(Product product, ProductRequestDTO request) {
        product.setName(request.getName());
        product.setProductCode(request.getProductCode());
        product.setSearchTag(request.getSearchTag());
        product.setDescription(request.getDescription());
        product.setSlug(request.getSlug());
        product.setStatus(request.getStatus() != null ? request.getStatus() : "ACTIVE"); // Mặc định là ACTIVE
        
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResouceNotFoundException("Category not found with id: " + request.getCategoryId()));
            product.setCategory(category);
        }
        
        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new ResouceNotFoundException("Brand not found with id: " + request.getBrandId()));
            product.setBrand(brand);
        }
        return product;
    }

    private ProductDTO mapToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setProductCode(product.getProductCode());
        dto.setSearchTag(product.getSearchTag());
        dto.setDescription(product.getDescription());
        dto.setSlug(product.getSlug());
        dto.setStatus(product.getStatus());
        
        if (product.getCategory() != null) {
            dto.setCategoryName(product.getCategory().getName());
        }
        if (product.getBrand() != null) {
            dto.setBrandName(product.getBrand().getName());
        }
        return dto;
    }



}
