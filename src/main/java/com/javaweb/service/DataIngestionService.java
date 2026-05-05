package com.javaweb.service;

import com.javaweb.entity.Blog;
import com.javaweb.entity.Product;
import com.javaweb.entity.StorePolicy;
import com.javaweb.repository.BlogRepository;
import com.javaweb.repository.ProductRepository;
import com.javaweb.repository.StorePolicyRepository;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DataIngestionService {

    private static final Logger log = LoggerFactory.getLogger(DataIngestionService.class);

    private final ProductRepository productRepository;
    private final BlogRepository blogRepository;
    private final StorePolicyRepository storePolicyRepository;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    public DataIngestionService(ProductRepository productRepository,
                                BlogRepository blogRepository,
                                StorePolicyRepository storePolicyRepository,
                                EmbeddingModel embeddingModel,
                                EmbeddingStore<TextSegment> embeddingStore) {
        this.productRepository = productRepository;
        this.blogRepository = blogRepository;
        this.storePolicyRepository = storePolicyRepository;
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
    }

    // Tự động chạy ngay khi khởi động và sau đó cứ mỗi 5 phút chạy một lần
    @PostConstruct
    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void autoIngest() {
        log.info(">>> Đang kiểm tra dữ liệu mới để nạp vào kho tri thức AI...");
        List<Document> documents = new ArrayList<>();

        // 1. Sản phẩm chưa được vectorized
        List<Product> newProducts = productRepository.findByIsVectorizedFalse();
        for (Product p : newProducts) {
            StringBuilder sb = new StringBuilder();
            sb.append("Loại: Sản phẩm\n");
            sb.append("Tên: ").append(p.getName() != null ? p.getName() : "").append("\n");
            sb.append("Mã sản phẩm: ").append(p.getProductCode() != null ? p.getProductCode() : "").append("\n");
            sb.append("Danh mục: ").append(p.getCategory() != null ? p.getCategory().getName() : "Không rõ").append("\n");
            sb.append("Thương hiệu: ").append(p.getBrand() != null ? p.getBrand().getName() : "Không rõ").append("\n");
            sb.append("Mô tả: ").append(p.getDescription() != null ? p.getDescription() : "").append("\n");

            // Thêm thông tin biến thể (size/màu/giá)
            if (p.getProductVariants() != null && !p.getProductVariants().isEmpty()) {
                sb.append("Các phiên bản có sẵn:\n");
                p.getProductVariants().forEach(v ->
                    sb.append("  - Size ").append(v.getSize())
                      .append(", Màu ").append(v.getColor())
                      .append(", Giá ").append(v.getPrice()).append("đ")
                      .append(", Tồn kho: ").append(v.getStockQuantity()).append("\n")
                );
            }

            Metadata metadata = Metadata.from("type", "product").put("id", p.getId().toString());
            documents.add(Document.from(sb.toString(), metadata));
            p.setIsVectorized(true);
        }

        // 2. Bài viết Blog chưa được vectorized
        List<Blog> newBlogs = blogRepository.findByIsVectorizedFalse();
        for (Blog b : newBlogs) {
            String text = String.format(
                "Loại: Bài viết\nTiêu đề: %s\nChủ đề: %s\nMôn thể thao: %s\nTác giả: %s\nTóm tắt: %s\nNội dung: %s",
                b.getTitle() != null ? b.getTitle() : "",
                b.getCategory() != null ? b.getCategory() : "",
                b.getSport() != null ? b.getSport() : "",
                b.getAuthor() != null ? b.getAuthor() : "",
                b.getExcerpt() != null ? b.getExcerpt() : "",
                b.getContent() != null ? b.getContent() : "");

            Metadata metadata = Metadata.from("type", "blog").put("id", b.getId().toString());
            documents.add(Document.from(text, metadata));
            b.setIsVectorized(true);
        }

        // 3. Chính sách cửa hàng chưa được vectorized (MỚI)
        List<StorePolicy> newPolicies = storePolicyRepository.findByIsVectorizedFalse();
        for (StorePolicy sp : newPolicies) {
            String text = String.format(
                "Loại: Chính sách/Hỗ trợ\nTiêu đề: %s\nNhóm: %s\nNội dung:\n%s",
                sp.getTitle() != null ? sp.getTitle() : "",
                sp.getCategory() != null ? sp.getCategory() : "",
                sp.getContent() != null ? sp.getContent() : "");

            Metadata metadata = Metadata.from("type", "policy").put("id", sp.getId().toString()).put("key", sp.getPolicyKey());
            documents.add(Document.from(text, metadata));
            sp.setIsVectorized(true);
        }

        if (!documents.isEmpty()) {
            try {
                EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                        .embeddingModel(embeddingModel)
                        .embeddingStore(embeddingStore)
                        .build();

                ingestor.ingest(documents);

                productRepository.saveAll(newProducts);
                blogRepository.saveAll(newBlogs);
                storePolicyRepository.saveAll(newPolicies);

                log.info(">>> ĐÃ NẠP THÀNH CÔNG {} tài liệu mới vào AI (trong đó {} sản phẩm, {} blog, {} chính sách).",
                        documents.size(), newProducts.size(), newBlogs.size(), newPolicies.size());
            } catch (Exception e) {
                log.error("!!! Lỗi khi nạp dữ liệu AI: {}", e.getMessage());
            }
        } else {
            log.info(">>> Không có dữ liệu mới. Kho tri thức AI đã được cập nhật.");
        }
    }
}
