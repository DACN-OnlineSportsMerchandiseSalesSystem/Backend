package com.javaweb.service;

import com.javaweb.entity.Blog;
import com.javaweb.entity.Product;
import com.javaweb.repository.BlogRepository;
import com.javaweb.repository.ProductRepository;
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
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    public DataIngestionService(ProductRepository productRepository, BlogRepository blogRepository, EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore) {
        this.productRepository = productRepository;
        this.blogRepository = blogRepository;
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
    }

    // Tự động chạy ngay khi khởi động và sau đó cứ mỗi 5 phút (300.000 ms) chạy một lần
    @PostConstruct
    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void autoIngest() {
        log.info(">>> Đang kiểm tra dữ liệu mới để nạp vào kho tri thức AI...");
        List<Document> documents = new ArrayList<>();

        // 1. Chỉ lấy những Sản phẩm MỚI (chưa được vectorized)
        List<Product> newProducts = productRepository.findByIsVectorizedFalse();
        for (Product p : newProducts) {
            String text = String.format("Sản phẩm: %s\nDanh mục: %s\nThương hiệu: %s\nMô tả: %s",
                    p.getName() != null ? p.getName() : "",
                    p.getCategory() != null ? p.getCategory().getName() : "Không rõ",
                    p.getBrand() != null ? p.getBrand().getName() : "Không rõ",
                    p.getDescription() != null ? p.getDescription() : "");
            
            Metadata metadata = Metadata.from("type", "product").put("id", p.getId().toString());
            documents.add(Document.from(text, metadata));
            
            // Đánh dấu đã nạp xong
            p.setIsVectorized(true);
        }

        // 2. Chỉ lấy những Bài viết MỚI (chưa được vectorized)
        List<Blog> newBlogs = blogRepository.findByIsVectorizedFalse();
        for (Blog b : newBlogs) {
            String text = String.format("Bài viết: %s\nChủ đề: %s\nMôn thể thao: %s\nTác giả: %s\nTóm tắt: %s\nNội dung: %s",
                    b.getTitle() != null ? b.getTitle() : "",
                    b.getCategory() != null ? b.getCategory() : "",
                    b.getSport() != null ? b.getSport() : "",
                    b.getAuthor() != null ? b.getAuthor() : "",
                    b.getExcerpt() != null ? b.getExcerpt() : "",
                    b.getContent() != null ? b.getContent() : "");
            
            Metadata metadata = Metadata.from("type", "blog").put("id", b.getId().toString());
            documents.add(Document.from(text, metadata));
            
            // Đánh dấu đã nạp xong
            b.setIsVectorized(true);
        }

        if (!documents.isEmpty()) {
            try {
                EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                        .embeddingModel(embeddingModel)
                        .embeddingStore(embeddingStore)
                        .build();
                
                ingestor.ingest(documents);
                
                // Lưu lại trạng thái đã nạp vào DB
                productRepository.saveAll(newProducts);
                blogRepository.saveAll(newBlogs);
                
                log.info(">>> ĐÃ TỰ ĐỘNG NẠP THÀNH CÔNG {} tài liệu mới vào AI.", documents.size());
            } catch (Exception e) {
                log.error("!!! Lỗi khi nạp dữ liệu AI: {}", e.getMessage());
            }
        } else {
            log.info(">>> Không tìm thấy dữ liệu mới. Kho tri thức AI đã được cập nhật.");
        }
    }
}
