package com.javaweb.service;

import com.javaweb.dto.ProductDTO;
import com.javaweb.dto.ProductRequestDTO;
import java.util.List;

public interface ProductService {
    List<ProductDTO> getAllProducts();
    ProductDTO getProductById(Long id);
    ProductDTO createProduct(ProductRequestDTO request);
    ProductDTO updateProduct(Long id, ProductRequestDTO request);
    void deleteProduct(Long id);
}
