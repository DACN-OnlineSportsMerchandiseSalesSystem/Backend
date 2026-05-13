package com.javaweb.service.impl;

import com.javaweb.dto.ProductDTO;
import com.javaweb.dto.ProductRequestDTO;
import com.javaweb.dto.ProductImageDTO;
import com.javaweb.dto.ProductVariantDTO;
import com.javaweb.entity.Product;
import com.javaweb.entity.Category;
import com.javaweb.entity.Brand;
import com.javaweb.repository.ProductRepository;
import com.javaweb.repository.CategoryRepository;
import com.javaweb.repository.BrandRepository;
import com.javaweb.service.ProductService;
import com.javaweb.exception.ResouceNotFoundException;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    @Override
    public List<ProductDTO> searchProductsAi(String query) {
        // 1. Chuyển query của khách hàng thành Vector
        Embedding queryEmbedding = embeddingModel.embed(query).content();

        // 2. Tìm kiếm các sản phẩm có Vector "gần giống" nhất (Độ tương đồng > 0.6)
        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(queryEmbedding, 10, 0.6);

        // 3. Lấy danh sách ID sản phẩm từ kết quả tìm được
        List<Long> ids = matches.stream()
                .filter(match -> "product".equals(match.embedded().metadata().getString("type")))
                .map(match -> Long.parseLong(match.embedded().metadata().getString("id")))
                .collect(Collectors.toList());

        if (ids.isEmpty()) {
            return new ArrayList<>();
        }

        // 4. Truy vấn MySQL để lấy thông tin chi tiết (giá, ảnh, tên...)
        List<Product> products = productRepository.findAllById(ids);
        
        // Duy trì thứ tự sắp xếp theo độ liên quan mà AI đã trả về
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        
        return ids.stream()
                .filter(productMap::containsKey)
                .map(id -> mapToDTO(productMap.get(id)))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getProductsByBrand(Long brandId) {
        return productRepository.findByBrandId(brandId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getProductsByCategoryAndBrand(Long categoryId, Long brandId) {
        return productRepository.findByCategoryIdAndBrandId(categoryId, brandId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
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
                    .orElseThrow(() -> new ResouceNotFoundException(
                            "Category not found with id: " + request.getCategoryId()));
            product.setCategory(category);
        }

        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(
                            () -> new ResouceNotFoundException("Brand not found with id: " + request.getBrandId()));
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

        // Mapping Images
        if (product.getProductImages() != null) {
            dto.setImages(product.getProductImages().stream().map(img -> {
                ProductImageDTO imgDto = new ProductImageDTO();
                imgDto.setId(img.getId());
                imgDto.setImageUrl(img.getImageUrl());
                imgDto.setIsThumbnail(img.getIsThumbnail());
                return imgDto;
            }).collect(Collectors.toList()));
        }

        // Mapping Variants
        if (product.getProductVariants() != null) {
            dto.setVariants(product.getProductVariants().stream().map(v -> {
                ProductVariantDTO vDto = new ProductVariantDTO();
                vDto.setId(v.getId());
                vDto.setSkuCode(v.getSkuCode());
                vDto.setSize(v.getSize());
                vDto.setColor(v.getColor());
                vDto.setPrice(v.getPrice());
                vDto.setStockQuantity(v.getStockQuantity());
                return vDto;
            }).collect(Collectors.toList()));
            
            // Lấy giá của biến thể đầu tiên làm giá đại diện
            if (dto.getVariants() != null && !dto.getVariants().isEmpty()) {
                dto.setPrice(dto.getVariants().get(0).getPrice());
            }
        }

        // Mapping Reviews Info
        if (product.getReviews() != null && !product.getReviews().isEmpty()) {
            dto.setReviewCount(product.getReviews().size());
            double avgRating = product.getReviews().stream()
                    .mapToInt(r -> r.getRating() != null ? r.getRating() : 0)
                    .average()
                    .orElse(0.0);
            dto.setRating(avgRating);
        } else {
            dto.setReviewCount(0);
            dto.setRating(0.0);
        }

        return dto;
    }

}
