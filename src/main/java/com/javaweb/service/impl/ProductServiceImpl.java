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
import com.javaweb.repository.OrderRepository;
import com.javaweb.repository.UserRepository;
import com.javaweb.entity.Orders;
import com.javaweb.entity.OrderItems;
import com.javaweb.entity.User;
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
import com.javaweb.entity.Discount;
import com.javaweb.repository.DiscountRepository;
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
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final DiscountRepository discountRepository;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final ProductVectorSyncService vectorSyncService;

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
        return productRepository.findByCategories_Id(categoryId)
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
        return productRepository.findByCategories_IdAndBrandId(categoryId, brandId)
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
        Product saved = productRepository.save(mapToEntity(product, request));
        vectorSyncService.syncProduct(saved); // CDC: Đồng bộ Vector DB ngay lập tức
        return mapToDTO(saved);
    }

    @Override
    public ProductDTO updateProduct(Long id, ProductRequestDTO request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResouceNotFoundException("Product not found with id: " + id));
        Product saved = productRepository.save(mapToEntity(product, request));
        vectorSyncService.syncProduct(saved); // CDC: Cập nhật Vector DB khi sửa sản phẩm
        return mapToDTO(saved);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResouceNotFoundException("Product not found with id: " + id));
        product.setStatus("INACTIVE");
        productRepository.save(product);
        // Không cần sync Vector DB khi xóa mềm vì AI sẽ dùng SQL context thật
    }

    private Product mapToEntity(Product product, ProductRequestDTO request) {
        product.setName(request.getName());
        product.setProductCode(request.getProductCode());
        product.setSearchTag(request.getSearchTag());
        product.setDescription(request.getDescription());
        product.setSlug(request.getSlug());
        product.setStatus(request.getStatus() != null ? request.getStatus() : "ACTIVE"); // Mặc định là ACTIVE

        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            Set<Category> categories = new HashSet<>(categoryRepository.findAllById(request.getCategoryIds()));
            product.setCategories(categories);
        } else {
            product.setCategories(new HashSet<>());
        }

        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(
                            () -> new ResouceNotFoundException("Brand not found with id: " + request.getBrandId()));
            product.setBrand(brand);
        } else {
            product.setBrand(null);
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

        if (product.getCategories() != null && !product.getCategories().isEmpty()) {
            dto.setCategoryIds(product.getCategories().stream().map(Category::getId).collect(Collectors.toList()));
            dto.setCategoryNames(product.getCategories().stream().map(Category::getName).collect(Collectors.toList()));
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

                // Logic Khóa giá (Dynamic Discount Calculation)
                List<Discount> activeDiscounts = discountRepository.findAllActiveDiscounts(new Date());
                int effectiveDiscount = activeDiscounts.stream()
                        .filter(d -> {
                            if (d.getScope() == DiscountScope.GLOBAL) return true;
                            if (d.getScope() == DiscountScope.BRAND && product.getBrand() != null
                                    && product.getBrand().getId().equals(d.getBrand() != null ? d.getBrand().getId() : null))
                                return true;
                            if (d.getScope() == DiscountScope.CATEGORY && d.getCategory() != null
                                    && product.getCategories() != null)
                                return product.getCategories().stream()
                                        .anyMatch(c -> c.getId().equals(d.getCategory().getId()));
                            return false;
                        })
                        .mapToInt(Discount::getDiscountPercent)
                        .max()
                        .orElse(0);

                vDto.setDiscount(effectiveDiscount);
                vDto.setOriginalPrice(v.getOriginalPrice());

                if (v.getOriginalPrice() != null) {
                    BigDecimal calculatedPrice = v.getOriginalPrice()
                            .multiply(BigDecimal.valueOf(100 - effectiveDiscount))
                            .divide(BigDecimal.valueOf(100));
                    vDto.setPrice(calculatedPrice);
                } else {
                    vDto.setPrice(v.getPrice());
                }

                vDto.setStockQuantity(v.getStockQuantity());
                return vDto;
            }).collect(Collectors.toList()));

            // Lấy giá của biến thể đầu tiên làm giá đại diện
            if (dto.getVariants() != null && !dto.getVariants().isEmpty()) {
                dto.setPrice(dto.getVariants().get(0).getPrice());
                dto.setOriginalPrice(dto.getVariants().get(0).getOriginalPrice());
                dto.setDiscount(dto.getVariants().get(0).getDiscount());
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

    @Override
    public List<ProductDTO> getTopSellingProductsPublic(int limit) {
        List<Long> productIds = productRepository.findTopSellingProductIds(PageRequest.of(0, limit));
        if (productIds.isEmpty()) return new ArrayList<>();

        List<Product> topProducts = productRepository.findAllById(productIds);

        Map<Long, Product> productMap = topProducts.stream().collect(Collectors.toMap(Product::getId, p -> p));
        List<Product> orderedProducts = new ArrayList<>();
        for (Long id : productIds) {
            if (productMap.containsKey(id)) {
                orderedProducts.add(productMap.get(id));
            }
        }

        return orderedProducts.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getPersonalizedRecommendations(String email, int limit) {
        if (email == null || email.isEmpty()) {
            return getTopSellingProductsPublic(limit);
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return getTopSellingProductsPublic(limit);
        }

        List<Orders> orders = orderRepository.findByUserId(userOpt.get().getId());
        if (orders.isEmpty()) {
            return getTopSellingProductsPublic(limit);
        }

        Map<Long, Long> categoryCount = new HashMap<>();
        Map<Long, Long> brandCount = new HashMap<>();
        Set<Long> boughtProductIds = new HashSet<>();

        for (Orders order : orders) {
            if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.DELIVERED) {
                for (OrderItems item : order.getOrderItems()) {
                    if (item.getProductVariants() != null && item.getProductVariants().getProducts() != null) {
                        Product p = item.getProductVariants().getProducts();
                        boughtProductIds.add(p.getId());

                        if (p.getBrand() != null) {
                            brandCount.put(p.getBrand().getId(), brandCount.getOrDefault(p.getBrand().getId(), 0L) + 1);
                        }
                        if (p.getCategories() != null) {
                            for (Category cat : p.getCategories()) {
                                categoryCount.put(cat.getId(), categoryCount.getOrDefault(cat.getId(), 0L) + 1);
                            }
                        }
                    }
                }
            }
        }

        Long topCategoryId = categoryCount.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
        Long topBrandId = brandCount.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);

        List<Product> recommendedProducts = new ArrayList<>();

        if (topCategoryId != null) {
            recommendedProducts.addAll(productRepository.findByCategories_Id(topCategoryId));
        }
        if (topBrandId != null) {
            recommendedProducts.addAll(productRepository.findByBrandId(topBrandId));
        }

        List<ProductDTO> result = recommendedProducts.stream()
                .filter(p -> !boughtProductIds.contains(p.getId()))
                .distinct()
                .limit(limit)
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        if (result.isEmpty()) {
            return getTopSellingProductsPublic(limit);
        }

        return result;
    }

}
