package com.javaweb.service.impl;

import com.javaweb.dto.ProductDTO;
import com.javaweb.dto.ProductRequestDTO;
import com.javaweb.dto.ProductImageDTO;
import com.javaweb.dto.ProductVariantDTO;
import com.javaweb.entity.*;
import com.javaweb.repository.*;
import com.javaweb.service.ProductService;
import com.javaweb.service.ProductVectorSyncService;
import com.javaweb.service.ProductElasticsearchSyncService;
import com.javaweb.document.ProductDocument;
import com.javaweb.exception.ResouceNotFoundException;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.StringQuery;

import com.javaweb.enums.OrderStatus;
import com.javaweb.enums.DiscountScope;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.redis.core.StringRedisTemplate;
import dev.langchain4j.model.chat.ChatLanguageModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaweb.dto.AiRecommendationResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Collections;
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
    private final ProductElasticsearchSyncService elasticsearchSyncService;
    private final ElasticsearchOperations elasticsearchOperations;
    private final StringRedisTemplate redisTemplate;
    private final ChatLanguageModel chatLanguageModel;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<ProductDTO> searchProductsAi(String query) {
        // 1. Tìm kiếm chính xác và tìm kiếm mờ (Full-text & Fuzzy Search) bằng Elasticsearch
        List<Long> esProductIds = new ArrayList<>();
        try {
            String jsonQuery = "{\n" +
                    "  \"bool\": {\n" +
                    "    \"must\": [\n" +
                    "      {\n" +
                    "        \"multi_match\": {\n" +
                    "          \"query\": \"" + query.replace("\"", "\\\"") + "\",\n" +
                    "          \"fields\": [\"name^3\", \"searchTag^2\", \"description\", \"brandName\"],\n" +
                    "          \"fuzziness\": \"AUTO\"\n" +
                    "        }\n" +
                    "      }\n" +
                    "    ],\n" +
                    "    \"filter\": [\n" +
                    "      {\n" +
                    "        \"term\": {\n" +
                    "          \"status\": \"ACTIVE\"\n" +
                    "        }\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  }\n" +
                    "}";
            Query esQuery = new StringQuery(jsonQuery);
            SearchHits<ProductDocument> hits = elasticsearchOperations.search(esQuery, ProductDocument.class);

            esProductIds = hits.stream()
                    .map(hit -> Long.parseLong(hit.getContent().getId()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Fallback sang MySQL search bằng LIKE nếu Elasticsearch có sự cố hoặc chưa khởi động
            List<Product> keywordProducts = productRepository.searchByKeyword(query);
            esProductIds = keywordProducts.stream().map(Product::getId).collect(Collectors.toList());
        }

        // 2. Tìm kiếm theo Ngữ nghĩa AI (Vector Search)
        Embedding queryEmbedding = embeddingModel.embed(query).content();
        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(queryEmbedding, 10, 0.6);

        List<Long> aiIds = matches.stream()
                .filter(match -> "product".equals(match.embedded().metadata().getString("type")))
                .map(match -> Long.parseLong(match.embedded().metadata().getString("id")))
                .collect(Collectors.toList());

        // 3. Kết hợp (Hybrid Search) - Ưu tiên Elasticsearch trước, sau đó bổ sung AI
        List<Long> mergedIds = new ArrayList<>(esProductIds);
        for (Long aiId : aiIds) {
            if (!mergedIds.contains(aiId)) {
                mergedIds.add(aiId);
            }
        }

        if (mergedIds.isEmpty()) return new ArrayList<>();

        List<Product> allProducts = productRepository.findAllById(mergedIds);
        Map<Long, Product> productMap = allProducts.stream().collect(Collectors.toMap(Product::getId, p -> p));

        // Trả về kết quả đã được sắp xếp độ ưu tiên
        return mergedIds.stream()
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
        List<Long> allCategoryIds = new ArrayList<>();
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResouceNotFoundException("Category not found"));
        
        getCategoryIdsRecursive(category, allCategoryIds);
        
        return productRepository.findByCategories_IdIn(allCategoryIds)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private void getCategoryIdsRecursive(Category category, List<Long> ids) {
        ids.add(category.getId());
        if (category.getSubCategories() != null) {
            for (Category sub : category.getSubCategories()) {
                getCategoryIdsRecursive(sub, ids);
            }
        }
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
        try {
            elasticsearchSyncService.syncProduct(saved);
        } catch (Exception e) {
            System.err.println("Elasticsearch Sync Error: " + e.getMessage());
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
        try {
            elasticsearchSyncService.syncProduct(saved);
        } catch (Exception e) {
            System.err.println("Elasticsearch Sync Error: " + e.getMessage());
        }
        return mapToDTO(saved);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ResouceNotFoundException("Product not found"));
        product.setStatus("INACTIVE");
        Product saved = productRepository.save(product);
        try {
            elasticsearchSyncService.syncProduct(saved);
        } catch (Exception e) {
            System.err.println("Elasticsearch Sync Error: " + e.getMessage());
        }
    }

    private Product mapToEntity(Product product, ProductRequestDTO request) {
        product.setName(request.getName());
        product.setProductCode(request.getProductCode());
        product.setSearchTag(request.getSearchTag());
        product.setDescription(request.getDescription());
        product.setSlug(request.getSlug());
        product.setStatus(request.getStatus() != null ? request.getStatus() : "ACTIVE");

        // Hạng mục 2: Metadata mở rộng cho AI Recommendation
        if (request.getTargetSport() != null) product.setTargetSport(request.getTargetSport());
        if (request.getFitnessLevel() != null) product.setFitnessLevel(request.getFitnessLevel());
        if (request.getGenderTarget() != null) product.setGenderTarget(request.getGenderTarget());
        if (request.getSuitableFor() != null) product.setSuitableFor(request.getSuitableFor());
        if (request.getBenefits() != null) product.setBenefits(request.getBenefits());

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

        // Xây dựng lookup map tồn kho theo từng biến thể: "COLOR|SIZE" -> stockQuantity
        Map<String, Integer> variantStockMap = new HashMap<>();
        if (request.getVariantStocks() != null && !request.getVariantStocks().isEmpty()) {
            for (com.javaweb.dto.VariantStockDTO vs : request.getVariantStocks()) {
                if (vs.getColor() != null && vs.getSize() != null) {
                    variantStockMap.put(vs.getColor().trim() + "|" + vs.getSize().trim(), vs.getStockQuantity() != null ? vs.getStockQuantity() : 0);
                }
            }
        }

        int counter = 1;
        for (String c : colors) {
            for (String s : sizes) {
                ProductVariant variant = new ProductVariant();
                variant.setColor(c);
                variant.setSize(s);
                variant.setOriginalPrice(request.getOriginalPrice());
                variant.setDiscount(request.getDiscount());
                if (request.getOriginalPrice() != null) {
                    int disc = request.getDiscount() != null ? request.getDiscount() : 0;
                    BigDecimal price = request.getOriginalPrice().multiply(BigDecimal.valueOf(100 - disc)).divide(BigDecimal.valueOf(100));
                    variant.setPrice(price);
                }

                // Ưu tiên tồn kho riêng theo biến thể, fallback về stockQuantity chung
                String key = c.trim() + "|" + s.trim();
                Integer stock = variantStockMap.getOrDefault(key,
                    request.getStockQuantity() != null ? request.getStockQuantity() : 0);
                variant.setStockQuantity(stock);

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

        // Hạng mục 2: Metadata mở rộng cho AI Recommendation
        dto.setTargetSport(product.getTargetSport());
        dto.setFitnessLevel(product.getFitnessLevel());
        dto.setGenderTarget(product.getGenderTarget());
        dto.setSuitableFor(product.getSuitableFor());
        dto.setBenefits(product.getBenefits());

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
        if (ids.isEmpty()) {
            // Fallback: return any available products when there are no paid/completed orders yet
            return productRepository.findAll(PageRequest.of(0, limit)).getContent()
                    .stream().map(this::mapToDTO).collect(Collectors.toList());
        }
        return productRepository.findAllById(ids).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getPersonalizedRecommendations(String email, String sport, String level, String priceRange, int limit) {
        // Giai đoạn 1: Redis Caching
        String cacheKey;
        User user = null;
        if (email != null && !email.isEmpty()) {
            user = userRepository.findByEmail(email).orElse(null);
        }

        String resolvedSport = (sport != null && !sport.trim().isEmpty()) 
                ? sport 
                : (user != null && user.getSports() != null && !user.getSports().trim().isEmpty() 
                        ? user.getSports().split(",")[0].trim() 
                        : "running");

        if (user != null) {
            String widgetSuffix = (sport != null ? ":" + sport : "") + (level != null ? ":" + level : "") + (priceRange != null ? ":" + priceRange : "");
            cacheKey = "rec:home:" + user.getId() + widgetSuffix;
        } else {
            cacheKey = "rec:home:guest:" + (sport != null ? sport : "default") + ":" + (level != null ? level : "default") + ":" + (priceRange != null ? priceRange : "default");
        }

        try {
            String cachedJson = redisTemplate.opsForValue().get(cacheKey);
            if (cachedJson != null && !cachedJson.isEmpty()) {
                List<ProductDTO> cachedList = objectMapper.readValue(cachedJson, new com.fasterxml.jackson.core.type.TypeReference<List<ProductDTO>>() {});
                if (cachedList != null && !cachedList.isEmpty()) {
                    System.out.println(">>> [AI Recommendation] Cache Hit: Serving from Redis for key: " + cacheKey);
                    return cachedList;
                }
            }
        } catch (Exception e) {
            System.err.println(">>> [AI Recommendation] Redis Cache Read Error: " + e.getMessage());
        }

        // Giai đoạn 2: Tạo Prompt và gọi Gemini
        List<ProductDTO> recommendedProducts = new ArrayList<>();
        try {
            // 2.1 Xây dựng cấu trúc Context gửi vào Prompt
            Map<String, Object> promptContext = new HashMap<>();
            promptContext.put("user_id", user != null ? "usr_" + user.getId() : "guest");
            
            Map<String, Object> profileMap = new HashMap<>();
            if (user != null) {
                profileMap.put("sports", user.getSports() != null ? user.getSports().split(",") : new String[]{});
                profileMap.put("level", user.getSportLevel() != null ? user.getSportLevel() : "beginner");
                profileMap.put("wrist_strength", user.getWristStrength() != null ? user.getWristStrength() : "medium");
                profileMap.put("budget", user.getPreferredBudget() != null ? user.getPreferredBudget() : "medium");
                profileMap.put("injuries", user.getInjuries() != null ? user.getInjuries() : "none");
            } else {
                profileMap.put("sports", new String[]{});
                profileMap.put("level", "beginner");
                profileMap.put("wrist_strength", "medium");
                profileMap.put("budget", "medium");
                profileMap.put("injuries", "none");
            }
            promptContext.put("profile", profileMap);

            Map<String, Object> widgetMap = new HashMap<>();
            widgetMap.put("active_sport", resolvedSport);
            widgetMap.put("current_interest", level != null && !level.trim().isEmpty() ? level : "general");
            widgetMap.put("price_range", priceRange != null && !priceRange.trim().isEmpty() ? priceRange : "all");
            promptContext.put("home_widget_selection", widgetMap);

            List<Map<String, Object>> purchaseHistoryList = new ArrayList<>();
            if (user != null) {
                List<Orders> orders = orderRepository.findByUserId(user.getId());
                long ninetyDaysAgoMillis = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000);
                Date ninetyDaysAgo = new Date(ninetyDaysAgoMillis);
                for (Orders o : orders) {
                    if (o.getCreateAt() == null || o.getCreateAt().before(ninetyDaysAgo)) continue;
                    long daysAgo = (System.currentTimeMillis() - o.getCreateAt().getTime()) / (1000 * 60 * 60 * 24);
                    for (OrderItems item : o.getOrderItems()) {
                        if (item.getProductVariants() != null && item.getProductVariants().getProducts() != null) {
                            Product p = item.getProductVariants().getProducts();
                            Map<String, Object> historyItem = new HashMap<>();
                            historyItem.put("product_id", p.getId().toString());
                            historyItem.put("product_name", p.getName());
                            boolean isConsumable = p.getCategories().stream().anyMatch(cat -> cat.getIsConsumable() != null && cat.getIsConsumable());
                            historyItem.put("category", p.getCategories().isEmpty() ? "unknown" : p.getCategories().iterator().next().getName());
                            historyItem.put("is_consumable", isConsumable);
                            historyItem.put("days_ago", daysAgo);
                            purchaseHistoryList.add(historyItem);
                        }
                    }
                }
            }
            promptContext.put("purchase_history", purchaseHistoryList);

            // 2.2 Tạo Prompt
            String contextJson = objectMapper.writeValueAsString(promptContext);
            String prompt = "Bạn là chuyên gia tư vấn trang thiết bị thể thao cao cấp và hệ thống phân tích ý định mua sắm.\n" +
                    "Nhiệm vụ: Phân tích hồ sơ khách hàng, dữ liệu tương tác và lịch sử đơn hàng thể thao để sinh ra các tham số tìm kiếm chính xác.\n\n" +
                    "Quy tắc nghiệp vụ:\n" +
                    "1. LOẠI TRỪ (Negative Filter): Nếu khách đã mua \"Hàng bền\" (Vợt, Giày, Khung tập) trong vòng 90 ngày (is_consumable = false), thêm product_id và category đó vào mảng loại trừ (exclude). Không gợi ý lại món tương tự.\n" +
                    "2. BÁN CHÉO (Cross-selling): Đã mua Vợt -> Gợi ý Giày, Cước đan, Balo, Băng gối. Đã mua Giày chạy -> Gợi ý Tất nén cơ, Bình nước, Gel năng lượng.\n" +
                    "3. MUA BỔ SUNG (Replenishment): Nếu \"Hàng tiêu hao\" (Quấn cán, Cầu lông, Bóng, Whey, Bột điện giải) đã mua quá chu kỳ sử dụng trung bình (ngày mua > 30 ngày), ưu tiên gợi ý mua lại bằng cách đưa category của nó vào danh sách gợi ý.\n" +
                    "4. KHÁCH VÃNG LAI (Chưa đăng nhập / Guest): Nếu chưa có lịch sử đơn hàng (user_id = guest hoặc purchase_history rỗng), tập trung phân tích môn thể thao (active_sport), trình độ (current_interest) và mức giá trong home_widget_selection để gợi ý các sản phẩm chất lượng, bán chạy nhất và phù hợp nhất cho bộ môn đó.\n" +
                    "5. RÀNG BUỘC CHẤN THƯƠNG: Nếu hồ sơ người dùng có tiền sử chấn thương (ví dụ: đau khớp gối, lỏng cổ chân, viêm gân), ưu tiên gợi ý các phụ kiện bảo hộ thể thao (băng gối trợ lực, quấn cổ chân, giày đệm êm giảm chấn).\n" +
                    "6. GIÁ CẢ: Nếu home_widget_selection.price_range là 'all' hoặc rỗng, ĐẶT price_max là 0 (không giới hạn giá). Tuyệt đối không tự bịa ra giới hạn giá thấp làm loại bỏ các dụng cụ thi đấu cao cấp.\n" +
                    "7. Trả về đúng 1 đối tượng JSON duy nhất theo schema định sẵn dưới đây, không viết bất kỳ chữ giải thích nào khác ngoài JSON, không bọc trong ```json.\n\n" +
                    "Schema JSON mẫu:\n" +
                    "{\n" +
                    "  \"semantic_query\": \"chuỗi tìm kiếm tiếng Việt chứa từ khoá mô tả sản phẩm gợi ý\",\n" +
                    "  \"filters\": {\n" +
                    "    \"target_sports\": [\"tên các bộ môn đích bằng tiếng Việt hoặc tiếng Anh, ví dụ: 'badminton', 'running', 'gym', 'pickleball', 'football', 'bóng đá', 'cầu lông', 'chạy bộ'\"],\n" +
                    "    \"categories\": [\"tên các danh mục sản phẩm gợi ý, ví dụ: 'shoes', 'rackets', 'giày', 'vợt', 'quần áo', 'bảo vệ', 'phụ kiện'\"],\n" +
                    "    \"price_max\": 0,\n" +
                    "    \"exclude_product_ids\": [],\n" +
                    "    \"exclude_categories\": []\n" +
                    "  },\n" +
                    "  \"personalized_headline\": \"tiêu đề cá nhân hóa bằng tiếng Việt ngắn gọn để hiển thị lên UI\",\n" +
                    "  \"reason_badges\": {\n" +
                    "    \"tên_danh_mục_1\": \"lý do gợi ý ngắn gọn cho danh mục này\",\n" +
                    "    \"tên_danh_mục_2\": \"lý do gợi ý ngắn gọn\"\n" +
                    "  }\n" +
                    "}\n\n" +
                    "Dữ liệu đầu vào (Input Context JSON):\n" + contextJson;

            // 2.3 Gọi Gemini
            String response = chatLanguageModel.generate(prompt);
            System.out.println(">>> [AI Recommendation] Gemini Raw Response: " + response);

            String cleanJson = response.trim();
            if (cleanJson.startsWith("```")) {
                int firstBrace = cleanJson.indexOf("{");
                int lastBrace = cleanJson.lastIndexOf("}");
                if (firstBrace != -1 && lastBrace != -1) {
                    cleanJson = cleanJson.substring(firstBrace, lastBrace + 1);
                }
            }

            AiRecommendationResponse aiResponse = objectMapper.readValue(cleanJson, AiRecommendationResponse.class);

            // Giai đoạn 3: Thực hiện Hybrid Search + Re-ranking
            if (aiResponse != null && aiResponse.getSemanticQuery() != null) {
                List<EmbeddingMatch<TextSegment>> matches = new ArrayList<>();
                List<Long> matchedProductIds = new ArrayList<>();

                // Thử tìm kiếm ngữ nghĩa qua Vector Store (ChromaDB)
                try {
                    Embedding queryEmbedding = embeddingModel.embed(aiResponse.getSemanticQuery()).content();
                    matches = embeddingStore.findRelevant(queryEmbedding, 30, 0.4);
                    matchedProductIds = matches.stream()
                            .filter(match -> "product".equals(match.embedded().metadata().getString("type")))
                            .map(match -> Long.parseLong(match.embedded().metadata().getString("id")))
                            .collect(Collectors.toList());
                } catch (Exception vectorEx) {
                    System.out.println(">>> [AI Recommendation] Vector Store không sẵn sàng (" + vectorEx.getMessage() + "). Tự động chuyển sang tìm kiếm từ khóa ngữ nghĩa trên MySQL.");
                }

                Set<Product> merged = new LinkedHashSet<>();
                if (!matchedProductIds.isEmpty()) {
                    merged.addAll(productRepository.findAllById(matchedProductIds));
                }

                // 1. Tìm bằng chuỗi Semantic Query trực tiếp trên MySQL
                if (aiResponse.getSemanticQuery() != null && !aiResponse.getSemanticQuery().trim().isEmpty()) {
                    List<Product> keywordMatches = productRepository.searchByKeyword(aiResponse.getSemanticQuery().trim());
                    merged.addAll(keywordMatches);

                    // Tách từ khóa quan trọng (độ dài >= 3)
                    String[] words = aiResponse.getSemanticQuery().trim().split("\\s+");
                    for (String word : words) {
                        String cleanWord = word.replaceAll("[^a-zA-Z0-9À-ỹà-ỹ]", "").trim();
                        if (cleanWord.length() >= 3) {
                            merged.addAll(productRepository.searchByKeyword(cleanWord));
                        }
                    }
                }

                // 2. Tìm theo targetSports & categories với hỗ trợ song ngữ toàn diện
                List<String> targetSports = aiResponse.getFilters() != null && aiResponse.getFilters().getTargetSports() != null 
                        ? new ArrayList<>(aiResponse.getFilters().getTargetSports()) : new ArrayList<>();
                if (resolvedSport != null && !resolvedSport.trim().isEmpty() && !targetSports.contains(resolvedSport)) {
                    targetSports.add(resolvedSport);
                }
                List<String> targetCategories = aiResponse.getFilters() != null && aiResponse.getFilters().getCategories() != null 
                        ? new ArrayList<>(aiResponse.getFilters().getCategories()) : new ArrayList<>();

                List<Category> cats = categoryRepository.findAll();
                List<Long> targetCatIds = cats.stream()
                        .filter(c -> {
                            String cName = c.getName() != null ? c.getName().toLowerCase() : "";
                            String cSlug = c.getSlug() != null ? c.getSlug().toLowerCase() : "";

                            boolean matchSport = targetSports.stream().anyMatch(ts -> {
                                String t = ts.toLowerCase().trim();
                                return cName.contains(t) || cSlug.contains(t)
                                        || (t.contains("foot") && (cName.contains("bóng đá") || cSlug.contains("bong-da")))
                                        || (t.contains("bóng đá") && (cName.contains("bóng đá") || cSlug.contains("bong-da")))
                                        || (t.contains("badminton") && (cName.contains("cầu lông") || cSlug.contains("cau-long")))
                                        || (t.contains("cầu lông") && (cName.contains("cầu lông") || cSlug.contains("cau-long")))
                                        || (t.contains("run") && (cName.contains("chạy") || cSlug.contains("chay")))
                                        || (t.contains("chạy") && (cName.contains("chạy") || cSlug.contains("chay")))
                                        || (t.contains("pickle") && (cName.contains("pickleball") || cSlug.contains("pickleball")))
                                        || (t.contains("basket") && (cName.contains("bóng rổ") || cSlug.contains("bong-ro")))
                                        || (t.contains("bóng rổ") && (cName.contains("bóng rổ") || cSlug.contains("bong-ro")))
                                        || (t.contains("swim") && (cName.contains("bơi") || cSlug.contains("boi")))
                                        || (t.contains("bơi") && (cName.contains("bơi") || cSlug.contains("boi")))
                                        || (t.contains("cycl") && (cName.contains("xe đạp") || cSlug.contains("xe-dap")))
                                        || (t.contains("xe đạp") && (cName.contains("xe đạp") || cSlug.contains("xe-dap")))
                                        || (t.contains("yoga") && (cName.contains("yoga") || cSlug.contains("yoga")))
                                        || ((t.contains("box") || t.contains("martial") || t.contains("võ")) && (cName.contains("võ") || cName.contains("boxing") || cSlug.contains("vo-thuat")))
                                        || ((t.contains("gym") || t.contains("fit")) && (cName.contains("gym") || cSlug.contains("gym")));
                            });

                            boolean matchCat = targetCategories.stream().anyMatch(tc -> {
                                String t = tc.toLowerCase().trim();
                                return cName.contains(t) || cSlug.contains(t)
                                        || (t.contains("shoe") && (cName.contains("giày") || cSlug.contains("giay")))
                                        || (t.contains("giày") && (cName.contains("giày") || cSlug.contains("giay")))
                                        || ((t.contains("racket") || t.contains("paddle") || t.contains("vợt")) && (cName.contains("vợt") || cSlug.contains("vot")))
                                        || ((t.contains("cloth") || t.contains("apparel") || t.contains("áo") || t.contains("quần")) && (cName.contains("quần áo") || cName.contains("áo đấu")))
                                        || ((t.contains("ball") || t.contains("bóng")) && (cName.contains("bóng") || cSlug.contains("bong")))
                                        || ((t.contains("shin") || t.contains("protect") || t.contains("bảo vệ") || t.contains("bó cơ")) && (cName.contains("bảo vệ") || cName.contains("bó cơ") || cName.contains("phụ kiện")))
                                        || (t.contains("sock") && (cName.contains("tất") || cName.contains("vớ")))
                                        || (t.contains("glove") && cName.contains("găng"))
                                        || (t.contains("mat") && cName.contains("thảm"));
                            });

                            return matchSport || matchCat;
                        })
                        .map(Category::getId)
                        .collect(Collectors.toList());

                if (!targetCatIds.isEmpty()) {
                    merged.addAll(productRepository.findByCategories_IdIn(targetCatIds));
                }
                if (resolvedSport != null && !resolvedSport.trim().isEmpty()) {
                    merged.addAll(productRepository.findBySportOrTag(resolvedSport.trim()));
                }
                for (String ts : targetSports) {
                    if (ts != null && !ts.trim().isEmpty()) {
                        merged.addAll(productRepository.findBySportOrTag(ts.trim()));
                    }
                }

                if (merged.isEmpty()) {
                    merged.addAll(productRepository.findAll(PageRequest.of(0, 30)).getContent());
                }
                List<Product> candidates = new ArrayList<>(merged);


                AiRecommendationResponse.Filters filters = aiResponse.getFilters();
                List<Product> filteredCandidates = new ArrayList<>();
                boolean hasExplicitPriceLimit = priceRange != null && !priceRange.trim().isEmpty() && !"all".equalsIgnoreCase(priceRange.trim());
                for (Product p : candidates) {
                    if (!isProductActive(p)) continue;

                    if (filters != null && filters.getExcludeProductIds() != null &&
                            filters.getExcludeProductIds().contains(p.getId().toString())) {
                        continue;
                    }

                    if (filters != null && filters.getExcludeCategories() != null && !filters.getExcludeCategories().isEmpty()) {
                        boolean matchExcludeCat = p.getCategories().stream()
                                .anyMatch(c -> filters.getExcludeCategories().contains(c.getName()) || 
                                               filters.getExcludeCategories().contains(c.getSlug()));
                        if (matchExcludeCat) continue;
                    }

                    if (hasExplicitPriceLimit && filters != null && filters.getPriceMax() != null && filters.getPriceMax().compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal minPrice = p.getProductVariants().stream()
                                .map(ProductVariant::getPrice)
                                .filter(Objects::nonNull)
                                .min(BigDecimal::compareTo)
                                .orElse(BigDecimal.ZERO);
                        if (minPrice.compareTo(filters.getPriceMax()) > 0) continue;
                    }

                    int totalStock = p.getProductVariants().stream()
                            .mapToInt(v -> v.getStockQuantity() != null ? v.getStockQuantity() : 0)
                            .sum();
                    if (totalStock <= 0) continue;

                    filteredCandidates.add(p);
                }

                // Nếu bộ lọc quá chặt làm rỗng danh sách, nới lỏng (chỉ cần active và có hàng)
                if (filteredCandidates.isEmpty() && !candidates.isEmpty()) {
                    for (Product p : candidates) {
                        if (!isProductActive(p)) continue;
                        int totalStock = p.getProductVariants().stream()
                                .mapToInt(v -> v.getStockQuantity() != null ? v.getStockQuantity() : 0)
                                .sum();
                        if (totalStock > 0) {
                            filteredCandidates.add(p);
                        }
                    }
                }

                // Nếu vẫn rỗng (ví dụ tồn kho = 0), lấy bất kỳ sản phẩm active nào
                if (filteredCandidates.isEmpty()) {
                    for (Product p : candidates) {
                        if (isProductActive(p)) {
                            filteredCandidates.add(p);
                        }
                    }
                }

                List<Discount> activeDiscounts = discountRepository.findAllActiveDiscounts(new Date());
                Map<Product, Double> productScores = new HashMap<>();

                for (Product p : filteredCandidates) {
                    double simScore = 0.5;
                    if (matchedProductIds.contains(p.getId())) {
                        simScore = matches.stream()
                                .filter(m -> p.getId().toString().equals(m.embedded().metadata().getString("id")))
                                .mapToDouble(EmbeddingMatch::score)
                                .max()
                                .orElse(0.5);
                    }

                    double compScore = 0.0;
                    if (filters != null && filters.getCategories() != null) {
                        boolean matchesTargetCat = p.getCategories().stream()
                                .anyMatch(c -> filters.getCategories().contains(c.getName()) || 
                                               filters.getCategories().contains(c.getSlug()));
                        if (matchesTargetCat) compScore = 1.0;
                    }

                    double replenishScore = 0.0;
                    if (user != null) {
                        boolean isConsumable = p.getCategories().stream().anyMatch(cat -> cat.getIsConsumable() != null && cat.getIsConsumable());
                        if (isConsumable) {
                            boolean alreadyPurchased = purchaseHistoryList.stream()
                                    .anyMatch(h -> p.getId().toString().equals(h.get("product_id")));
                            if (alreadyPurchased) replenishScore = 1.0;
                        }
                    }

                    double popScore = p.getReviews() != null ? Math.min(1.0, p.getReviews().size() / 10.0) : 0.0;

                    double ratingScore = 0.0;
                    if (p.getReviews() != null && !p.getReviews().isEmpty()) {
                        double avgRating = p.getReviews().stream()
                                .mapToInt(r -> r.getRating() != null ? r.getRating() : 0)
                                .average()
                                .orElse(0.0);
                        ratingScore = avgRating / 5.0;
                    }

                    // Hạng mục 2: Tính điểm tương hợp công năng (suitableFor, targetSport, fitnessLevel)
                    double metaScore = 0.0;
                    if (filters != null) {
                        if (filters.getTargetSports() != null && p.getTargetSport() != null) {
                            boolean sportMatch = filters.getTargetSports().stream()
                                    .anyMatch(ts -> p.getTargetSport().toLowerCase().contains(ts.toLowerCase()));
                            if (sportMatch) metaScore += 0.5;
                        }
                        if (filters.getSuitableFor() != null && p.getSuitableFor() != null) {
                            boolean suitableMatch = filters.getSuitableFor().stream()
                                    .anyMatch(sf -> p.getSuitableFor().toLowerCase().contains(sf.toLowerCase()));
                            if (suitableMatch) metaScore += 0.5;
                        }
                    }

                    double directSportBoost = 0.0;
                    if (resolvedSport != null && !resolvedSport.trim().isEmpty()) {
                        String sLower = resolvedSport.toLowerCase().trim();
                        boolean targetSportMatch = p.getTargetSport() != null && p.getTargetSport().toLowerCase().contains(sLower);
                        boolean nameOrTagMatch = (p.getName() != null && p.getName().toLowerCase().contains(sLower))
                                || (p.getSearchTag() != null && p.getSearchTag().toLowerCase().contains(sLower));
                        if (targetSportMatch || nameOrTagMatch) {
                            directSportBoost = 1.0;
                        }
                    }

                    double finalScore = (0.25 * simScore) + (0.20 * compScore) + (0.25 * directSportBoost) + (0.15 * metaScore) + (0.10 * popScore) + (0.05 * ratingScore);
                    productScores.put(p, finalScore);
                }

                List<Product> finalSortedList = new ArrayList<>(productScores.keySet());
                finalSortedList.sort((p1, p2) -> Double.compare(productScores.get(p2), productScores.get(p1)));

                recommendedProducts = finalSortedList.stream()
                        .limit(limit)
                        .map(p -> {
                            ProductDTO dto = mapToDTO(p);
                            String reason = null;
                            if (aiResponse.getReasonBadges() != null) {
                                for (Category cat : p.getCategories()) {
                                    String cName = cat.getName().toLowerCase();
                                    for (Map.Entry<String, String> entry : aiResponse.getReasonBadges().entrySet()) {
                                        String badgeKey = entry.getKey().toLowerCase();
                                        if (cName.contains(badgeKey) || badgeKey.contains(cName)
                                                || (badgeKey.contains("shoe") && cName.contains("giày"))
                                                || (badgeKey.contains("foot") && cName.contains("bóng đá"))
                                                || (badgeKey.contains("sock") && cName.contains("tất"))
                                                || (badgeKey.contains("shin") && (cName.contains("phụ kiện") || cName.contains("bảo hộ")))) {
                                            reason = entry.getValue();
                                            break;
                                        }
                                    }
                                    if (reason != null) break;
                                }
                            }
                            if (reason == null) {
                                reason = "";
                            }
                            dto.setSearchTag("REASON:" + reason);
                            return dto;
                        })
                        .collect(Collectors.toList());

                if (!recommendedProducts.isEmpty() && aiResponse.getPersonalizedHeadline() != null) {
                    ProductDTO firstDto = recommendedProducts.get(0);
                    firstDto.setSearchTag("HEADLINE:" + aiResponse.getPersonalizedHeadline() + "||" + firstDto.getSearchTag());
                }

                if (!recommendedProducts.isEmpty()) {
                    try {
                        String listJson = objectMapper.writeValueAsString(recommendedProducts);
                        redisTemplate.opsForValue().set(cacheKey, listJson, 8, TimeUnit.HOURS);
                        System.out.println(">>> [AI Recommendation] Saved cache to Redis for key: " + cacheKey);
                    } catch (Exception cacheErr) {
                        System.err.println(">>> [AI Recommendation] Redis Cache Write Error: " + cacheErr.getMessage());
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println(">>> [AI Recommendation] Error in pipeline: " + ex.getMessage());
            ex.printStackTrace();
        }

        if (recommendedProducts.isEmpty()) {
            System.out.println(">>> [AI Recommendation] Pipeline failed or empty results. Invoking Fallback rule-based...");
            return getFallbackRecommendations(email, resolvedSport, limit);
        }

        return recommendedProducts;
    }

    private boolean isProductActive(Product p) {
        if (p == null) return false;
        String s = p.getStatus();
        return s == null || "active".equalsIgnoreCase(s) || "ACTIVE".equalsIgnoreCase(s);
    }

    private List<ProductDTO> getFallbackRecommendations(String email, String sport, int limit) {
        Set<Long> catIds = new HashSet<>();

        // 1. Ưu tiên tìm danh mục theo bộ môn đang chọn (cả guest lẫn logged-in)
        if (sport != null && !sport.trim().isEmpty()) {
            List<Category> allCats = categoryRepository.findAll();
            for (Category c : allCats) {
                String cName = c.getName() != null ? c.getName().toLowerCase() : "";
                String cSlug = c.getSlug() != null ? c.getSlug().toLowerCase() : "";
                String s = sport.toLowerCase().trim();
                if (cName.contains(s) || cSlug.contains(s)
                        || (s.contains("foot") && (cName.contains("bóng đá") || cSlug.contains("bong-da")))
                        || (s.contains("bóng đá") && (cName.contains("bóng đá") || cSlug.contains("bong-da")))
                        || (s.contains("badminton") && (cName.contains("cầu lông") || cSlug.contains("cau-long")))
                        || (s.contains("cầu lông") && (cName.contains("cầu lông") || cSlug.contains("cau-long")))
                        || (s.contains("run") && (cName.contains("chạy") || cSlug.contains("chay")))
                        || (s.contains("chạy") && (cName.contains("chạy") || cSlug.contains("chay")))
                        || (s.contains("gym") && (cName.contains("gym") || cSlug.contains("gym")))
                        || (s.contains("basket") && (cName.contains("bóng rổ") || cSlug.contains("bong-ro")))
                        || (s.contains("bóng rổ") && (cName.contains("bóng rổ") || cSlug.contains("bong-ro")))
                        || (s.contains("swim") && (cName.contains("bơi") || cSlug.contains("boi")))
                        || (s.contains("bơi") && (cName.contains("bơi") || cSlug.contains("boi")))
                        || (s.contains("pickle") && (cName.contains("pickleball") || cSlug.contains("pickleball")))) {
                    catIds.add(c.getId());
                }
            }
        }

        // 2. Nếu là user đã đăng nhập, bổ sung danh mục quan tâm & danh mục đã từng mua
        if (email != null && !email.isEmpty()) {
            Optional<User> u = userRepository.findByEmail(email);
            if (u.isPresent()) {
                if (u.get().getInterestedCategories() != null) {
                    u.get().getInterestedCategories().forEach(c -> catIds.add(c.getId()));
                }
                List<Orders> orders = orderRepository.findByUserId(u.get().getId());
                for (Orders o : orders) {
                    for (OrderItems i : o.getOrderItems()) {
                        if (i.getProductVariants() != null && i.getProductVariants().getProducts() != null) {
                            i.getProductVariants().getProducts().getCategories().forEach(c -> catIds.add(c.getId()));
                        }
                    }
                }
            }
        }

        // 3. Nếu vẫn trống (ví dụ guest chưa chọn môn hoặc môn chưa có trong DB)
        if (catIds.isEmpty()) {
            Optional<Category> chayBo = categoryRepository.findByName("Chạy bộ");
            chayBo.ifPresent(category -> catIds.add(category.getId()));
        }

        Set<Product> collectedProducts = new LinkedHashSet<>();
        if (!catIds.isEmpty()) {
            collectedProducts.addAll(productRepository.findByCategories_IdIn(new ArrayList<>(catIds)));
        }
        if (sport != null && !sport.trim().isEmpty()) {
            collectedProducts.addAll(productRepository.findBySportOrTag(sport.trim()));
        }

        if (!collectedProducts.isEmpty()) {
            List<Product> productList = new ArrayList<>(collectedProducts);
            List<ProductDTO> result = productList.stream()
                    .filter(this::isProductActive)
                    .limit(limit)
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
            if (!result.isEmpty()) {
                return result;
            }
        }

        return getTopSellingProductsPublic(limit);
    }

    /**
     * Tự động quét và đồng bộ toàn bộ sản phẩm từ MySQL sang Elasticsearch khi khởi động ứng dụng nếu index trống.
     */
    @org.springframework.context.event.EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void initElasticsearchIndexOnStartup() {
        try {
            org.springframework.data.elasticsearch.core.query.Query countQuery = 
                    new org.springframework.data.elasticsearch.core.query.StringQuery("{\"match_all\": {}}");
            long count = elasticsearchOperations.count(countQuery, ProductDocument.class);
            if (count == 0) {
                System.out.println(">>> [Elasticsearch] Khởi chạy đồng bộ dữ liệu ban đầu từ MySQL sang Elasticsearch...");
                List<Product> products = productRepository.findAll();
                for (Product p : products) {
                    elasticsearchSyncService.syncProduct(p);
                }
                System.out.println(">>> [Elasticsearch] Hoàn tất đồng bộ dữ liệu ban đầu. Đã đánh chỉ mục " + products.size() + " sản phẩm.");
            } else {
                System.out.println(">>> [Elasticsearch] Chỉ mục đã có " + count + " sản phẩm. Bỏ qua đồng bộ ban đầu.");
            }
        } catch (Exception e) {
            System.out.println(">>> [Elasticsearch] Không kết nối được Elasticsearch (cổng 9200 offline). Hệ thống tự động kích hoạt chế độ dự phòng MySQL Search + AI Vector Search.");
        }
    }
}
