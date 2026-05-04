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
import org.springframework.stereotype.Service;

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

    @PostConstruct
    public void init() {
        log.info("Bắt đầu nạp dữ liệu vào Vector Store...");
        List<Document> documents = new ArrayList<>();

        // 1. Nạp Products
        List<Product> products = productRepository.findAll();
        for (Product p : products) {
            String text = String.format("Sản phẩm: %s\nDanh mục: %s\nThương hiệu: %s\nMô tả: %s",
                    p.getName() != null ? p.getName() : "",
                    p.getCategory() != null ? p.getCategory().getName() : "Không rõ",
                    p.getBrand() != null ? p.getBrand().getName() : "Không rõ",
                    p.getDescription() != null ? p.getDescription() : "");
            
            Metadata metadata = Metadata.from("type", "product")
                    .put("id", p.getId().toString());
            
            documents.add(Document.from(text, metadata));
        }

        // 2. Nạp Blogs
        List<Blog> blogs = blogRepository.findAll();
        for (Blog b : blogs) {
            String text = String.format("Bài viết: %s\nChủ đề: %s\nMôn thể thao: %s\nTác giả: %s\nTóm tắt: %s\nNội dung: %s",
                    b.getTitle() != null ? b.getTitle() : "",
                    b.getCategory() != null ? b.getCategory() : "",
                    b.getSport() != null ? b.getSport() : "",
                    b.getAuthor() != null ? b.getAuthor() : "",
                    b.getExcerpt() != null ? b.getExcerpt() : "",
                    b.getContent() != null ? b.getContent() : "");
            
            Metadata metadata = Metadata.from("type", "blog")
                    .put("id", b.getId().toString());
            
            documents.add(Document.from(text, metadata));
        }

        if (!documents.isEmpty()) {
            try {
                EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                        .embeddingModel(embeddingModel)
                        .embeddingStore(embeddingStore)
                        .build();
                
                ingestor.ingest(documents);
                log.info("Đã nạp thành công {} tài liệu vào Vector Store.", documents.size());
            } catch (Exception e) {
                log.error("Lỗi khi nạp dữ liệu vào Vector Store: {}", e.getMessage());
            }
        } else {
            log.info("Không có dữ liệu để nạp.");
        }
    }
}
