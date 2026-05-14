package com.javaweb.service.impl;

import com.javaweb.dto.ProductDTO;
import com.javaweb.dto.ProductRequestDTO;
import com.javaweb.dto.ProductImageDTO;
import com.javaweb.dto.ProductVariantDTO;
import com.javaweb.entity.*;
import com.javaweb.repository.*;
import com.javaweb.service.ProductService;
import com.javaweb.service.ProductVectorSyncService;
import com.javaweb.exception.ResouceNotFoundException;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.javaweb.enums.OrderStatus;
import com.javaweb.enums.DiscountScope;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final DiscountRepository discountRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository productVariantRepository;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final ProductVectorSyncService vectorSyncService;

    @Override
    public List<ProductDTO> searchProductsAi(String query) {
        Embedding queryEmbedding = embeddingModel.embed(query).content();
        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(queryEmbedding, 10, 0.6);

        List<Long> ids = matches.stream()
                .filter(match -> "product".equals(match.embedded().metadata().getString("type")))
                .map(match -> Long.parseLong(match.embedded().metadata().getString("id")))
                .collect(Collectors.toList());

        if (ids.isEmpty()) return new ArrayList<>();

        List<Product> products = productRepository.findAllById(ids);
        Map<Long, Product> productMap = products.stream().collect(Collectors.toMap(Product::getId, p -> p));

        return ids.stream()
                .filter(productMap::containsKey)
                .map(id -> mapToDTO(productMap.get(id)))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategories_Id(categoryId).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getProductsByBrand(Long brandId) {
        return productRepository.findByBrandId(brandId).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getProductsByCategoryAndBrand(Long categoryId, Long brandId) {
        return productRepository.findByCategories_IdAndBrandId(categoryId, brandId).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ResouceNotFoundException("Product not found"));
        return mapToDTO(product);
    }

    @Override
    public ProductDTO createProduct(ProductRequestDTO request) {
        Product product = new Product();
        Product saved = productRepository.save(mapToEntity(product, request));
        try {
            vectorSyncService.syncProduct(saved);
        } catch (Exception e) {
            System.err.println("AI Sync Error: " + e.getMessage());
        }
        return mapToDTO(saved);
    }

    @Override
    public ProductDTO updateProduct(Long id, ProductRequestDTO request) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ResouceNotFoundException("Product not found"));
        Product saved = productRepository.save(mapToEntity(product, request));
        try {
            vectorSyncService.syncProduct(saved);
        } catch (Exception e) {
            System.err.println("AI Sync Error: " + e.getMessage());
        }
        return mapToDTO(saved);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ResouceNotFoundException("Product not found"));
        product.setStatus("INACTIVE");
        productRepository.save(product);
    }

    private Product mapToEntity(Product product, ProductRequestDTO request) {
        product.setName(request.getName());
        product.setProductCode(request.getProductCode());
        product.setSearchTag(request.getSearchTag());
        product.setDescription(request.getDescription());
        product.setSlug(request.getSlug());
        product.setStatus(request.getStatus() != null ? request.getStatus() : "ACTIVE");

        if (request.getCategoryIds() != null) {
            product.setCategories(new HashSet<>(categoryRepository.findAllById(request.getCategoryIds())));
        }

        if (request.getBrandId() != null) {
            product.setBrand(brandRepository.findById(request.getBrandId()).orElse(null));
        }

        // Cập nhật ảnh
        if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
            if (product.getProductImages() == null) product.setProductImages(new HashSet<>());
            product.getProductImages().clear(); // Nhờ orphanRemoval = true nên sẽ xóa trong DB
            
            ProductImage newImg = new ProductImage();
            newImg.setImageUrl(request.getImageUrl());
            newImg.setIsThumbnail(true);
            newImg.setProducts(product);
            product.getProductImages().add(newImg);
        }

        // Cập nhật biến thể
        if (product.getProductVariants() == null) product.setProductVariants(new HashSet<>());
        product.getProductVariants().clear();

        List<String> colors = (request.getColors() != null && !request.getColors().isEmpty()) ? request.getColors() : List.of("Default");
        List<String> sizes = (request.getSizes() != null && !request.getSizes().isEmpty()) ? request.getSizes() : List.of("Default");

        int counter = 1;
        for (String c : colors) {
            for (String s : sizes) {
                ProductVariant variant = new ProductVariant();
                variant.setColor(c);
                variant.setSize(s);
                variant.setOriginalPrice(request.getOriginalPrice());
                variant.setDiscount(request.getDiscount());
                variant.setStockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0);
                variant.setProducts(product);
                
                String baseCode = (product.getProductCode() != null && !product.getProductCode().isEmpty()) ? product.getProductCode() : "P" + (product.getId() != null ? product.getId() : "NEW");
                // Sử dụng UUID ngắn hoặc counter để đảm bảo SKU không bao giờ trùng trong cùng 1 lần lưu
                String sku = baseCode + "-" + c.toUpperCase() + "-" + s.toUpperCase() + "-" + java.util.UUID.randomUUID().toString().substring(0, 4).toUpperCase();
                variant.setSkuCode(sku);
                
                product.getProductVariants().add(variant);
            }
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

        if (product.getBrand() != null) {
            dto.setBrandName(product.getBrand().getName());
            dto.setBrandId(product.getBrand().getId());
        }

        if (product.getCategories() != null) {
            dto.setCategoryNames(product.getCategories().stream().map(Category::getName).collect(Collectors.toList()));
            dto.setCategoryIds(product.getCategories().stream().map(Category::getId).collect(Collectors.toList()));
        }

        if (product.getProductImages() != null) {
            dto.setImages(product.getProductImages().stream().map(img -> {
                ProductImageDTO d = new ProductImageDTO();
                d.setId(img.getId());
                d.setImageUrl(img.getImageUrl());
                d.setIsThumbnail(img.getIsThumbnail());
                return d;
            }).collect(Collectors.toList()));
        }

        if (product.getProductVariants() != null) {
            List<Discount> activeDiscounts = discountRepository.findAllActiveDiscounts(new Date());
            dto.setVariants(product.getProductVariants().stream().map(v -> {
                ProductVariantDTO vd = new ProductVariantDTO();
                vd.setId(v.getId());
                vd.setSkuCode(v.getSkuCode());
                vd.setSize(v.getSize());
                vd.setColor(v.getColor());
                vd.setOriginalPrice(v.getOriginalPrice());
                vd.setStockQuantity(v.getStockQuantity());

                // Lấy mức giảm giá lớn nhất giữa Giảm giá nhập tay và Chương trình khuyến mãi
                int manualDiscount = v.getDiscount() != null ? v.getDiscount() : 0;
                int promoDiscount = activeDiscounts.stream().filter(d -> {
                    if (d.getScope() == DiscountScope.GLOBAL) return true;
                    if (d.getScope() == DiscountScope.BRAND && product.getBrand() != null && product.getBrand().getId().equals(d.getBrand() != null ? d.getBrand().getId() : null)) return true;
                    if (d.getScope() == DiscountScope.CATEGORY && d.getCategory() != null && product.getCategories() != null)
                        return product.getCategories().stream().anyMatch(c -> c.getId().equals(d.getCategory().getId()));
                    return false;
                }).mapToInt(Discount::getDiscountPercent).max().orElse(0);

                int finalDiscount = Math.max(manualDiscount, promoDiscount);
                vd.setDiscount(finalDiscount);
                if (v.getOriginalPrice() != null) {
                    vd.setPrice(v.getOriginalPrice().multiply(BigDecimal.valueOf(100 - finalDiscount)).divide(BigDecimal.valueOf(100)));
                }
                return vd;
            }).collect(Collectors.toList()));

            if (!dto.getVariants().isEmpty()) {
                dto.setPrice(dto.getVariants().get(0).getPrice());
                dto.setOriginalPrice(dto.getVariants().get(0).getOriginalPrice());
                dto.setDiscount(dto.getVariants().get(0).getDiscount());
            }
        }

        if (product.getReviews() != null && !product.getReviews().isEmpty()) {
            dto.setReviewCount(product.getReviews().size());
            dto.setRating(product.getReviews().stream().mapToInt(r -> r.getRating() != null ? r.getRating() : 0).average().orElse(0.0));
        } else {
            dto.setReviewCount(0);
            dto.setRating(0.0);
        }

        return dto;
    }

    @Override
    public List<ProductDTO> getTopSellingProductsPublic(int limit) {
        List<Long> ids = productRepository.findTopSellingProductIds(PageRequest.of(0, limit));
        if (ids.isEmpty()) return new ArrayList<>();
        return productRepository.findAllById(ids).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getPersonalizedRecommendations(String email, int limit) {
        if (email == null || email.isEmpty()) return getTopSellingProductsPublic(limit);
        Optional<User> u = userRepository.findByEmail(email);
        if (u.isEmpty()) return getTopSellingProductsPublic(limit);

        List<Orders> orders = orderRepository.findByUserId(u.get().getId());
        if (orders.isEmpty()) return getTopSellingProductsPublic(limit);

        Set<Long> catIds = new HashSet<>();
        for (Orders o : orders) {
            for (OrderItems i : o.getOrderItems()) {
                if (i.getProductVariants() != null && i.getProductVariants().getProducts() != null) {
                    i.getProductVariants().getProducts().getCategories().forEach(c -> catIds.add(c.getId()));
                }
            }
        }

        if (catIds.isEmpty()) return getTopSellingProductsPublic(limit);
        return productRepository.findByCategories_IdIn(new ArrayList<>(catIds), PageRequest.of(0, limit)).stream().map(this::mapToDTO).collect(Collectors.toList());
    }
}
