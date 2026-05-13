package com.javaweb.service;

import com.javaweb.entity.Category;
import com.javaweb.entity.Product;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * CDC (Change Data Capture) Service
 * Tự động cập nhật Vector DB ngay khi sản phẩm được tạo/sửa từ Admin.
 * Giúp AI luôn "thông thạo" thông tin mới nhất mà không cần chờ scheduled job.
 */
@Service
public class ProductVectorSyncService {

    private static final Logger log = LoggerFactory.getLogger(ProductVectorSyncService.class);

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    public ProductVectorSyncService(EmbeddingModel embeddingModel,
                                    EmbeddingStore<TextSegment> embeddingStore) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
    }

    /**
     * Gọi hàm này sau khi lưu sản phẩm để cập nhật Vector DB ngay lập tức.
     */
    public void syncProduct(Product p) {
        try {
            String text = buildProductText(p);
            Metadata metadata = Metadata.from("type", "product").put("id", p.getId().toString());

            Document doc = Document.from(text, metadata);

            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .embeddingModel(embeddingModel)
                    .embeddingStore(embeddingStore)
                    .build();

            ingestor.ingest(List.of(doc));
            log.info(">>> [CDC] Đã đồng bộ sản phẩm '{}' (ID={}) vào Vector DB.", p.getName(), p.getId());
        } catch (Exception e) {
            log.error("!!! [CDC] Lỗi khi đồng bộ Vector DB cho sản phẩm ID={}: {}", p.getId(), e.getMessage());
        }
    }

    private String buildProductText(Product p) {
        StringBuilder sb = new StringBuilder();
        sb.append("Loại: Sản phẩm\n");
        sb.append("Tên: ").append(p.getName() != null ? p.getName() : "").append("\n");
        sb.append("Mã sản phẩm: ").append(p.getProductCode() != null ? p.getProductCode() : "").append("\n");
        sb.append("Danh mục: ").append(
                p.getCategories() != null && !p.getCategories().isEmpty()
                        ? p.getCategories().stream().map(Category::getName).collect(Collectors.joining(", "))
                        : "Không rõ"
        ).append("\n");
        sb.append("Thương hiệu: ").append(p.getBrand() != null ? p.getBrand().getName() : "Không rõ").append("\n");
        sb.append("Mô tả: ").append(p.getDescription() != null ? p.getDescription() : "").append("\n");

        if (p.getProductVariants() != null && !p.getProductVariants().isEmpty()) {
            sb.append("Các phiên bản có sẵn:\n");
            p.getProductVariants().forEach(v ->
                    sb.append("  - Size ").append(v.getSize())
                            .append(", Màu ").append(v.getColor())
                            .append(", Giá ").append(v.getPrice()).append("đ")
                            .append(", Tồn kho: ").append(v.getStockQuantity()).append("\n")
            );
        }
        return sb.toString();
    }
}
