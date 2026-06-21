package com.javaweb.service;

import com.javaweb.document.ProductDocument;
import com.javaweb.entity.Category;
import com.javaweb.entity.Product;
import com.javaweb.entity.ProductVariant;
import com.javaweb.repository.ProductElasticsearchRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductElasticsearchSyncService {

    private static final Logger log = LoggerFactory.getLogger(ProductElasticsearchSyncService.class);
    private final ProductElasticsearchRepository esRepository;

    /**
     * Đồng bộ thông tin sản phẩm từ MySQL sang Elasticsearch.
     */
    public void syncProduct(Product p) {
        try {
            ProductDocument doc = new ProductDocument();
            doc.setId(p.getId().toString());
            doc.setName(p.getName());
            doc.setProductCode(p.getProductCode());
            doc.setSearchTag(p.getSearchTag());
            doc.setDescription(p.getDescription());
            doc.setStatus(p.getStatus());
            doc.setSlug(p.getSlug());

            if (p.getBrand() != null) {
                doc.setBrandName(p.getBrand().getName());
            }

            if (p.getCategories() != null) {
                doc.setCategories(p.getCategories().stream()
                        .map(Category::getName)
                        .collect(Collectors.toList()));
            }

            if (p.getProductVariants() != null && !p.getProductVariants().isEmpty()) {
                doc.setColors(p.getProductVariants().stream()
                        .map(ProductVariant::getColor)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList()));

                doc.setSizes(p.getProductVariants().stream()
                        .map(ProductVariant::getSize)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList()));

                // Lấy giá trị nhỏ nhất của biến thể làm giá hiển thị/sắp xếp
                Double minPrice = p.getProductVariants().stream()
                        .map(ProductVariant::getPrice)
                        .filter(Objects::nonNull)
                        .map(BigDecimal::doubleValue)
                        .min(Double::compare)
                        .orElse(0.0);
                doc.setPrice(minPrice);
            } else {
                doc.setPrice(0.0);
            }

            esRepository.save(doc);
            log.info(">>> [Elasticsearch] Đã đồng bộ sản phẩm '{}' (ID={}) vào Elasticsearch.", p.getName(), p.getId());
        } catch (Exception e) {
            log.error("!!! [Elasticsearch] Lỗi khi đồng bộ sản phẩm ID={}: {}", p.getId(), e.getMessage());
        }
    }

    /**
     * Xóa sản phẩm khỏi Elasticsearch.
     */
    public void deleteProduct(Long id) {
        try {
            esRepository.deleteById(id.toString());
            log.info(">>> [Elasticsearch] Đã xóa sản phẩm ID={} khỏi Elasticsearch.", id);
        } catch (Exception e) {
            log.error("!!! [Elasticsearch] Lỗi khi xóa sản phẩm ID={} khỏi Elasticsearch: {}", id, e.getMessage());
        }
    }
}
