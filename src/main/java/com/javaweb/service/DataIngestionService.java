package com.javaweb.service;

import com.javaweb.entity.Blog;
import com.javaweb.entity.Category;
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
import java.util.stream.Collectors;

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

    @org.springframework.context.event.EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    @Transactional
    public void initOnStartup() {
        // Tự động khởi tạo tri thức ban đầu vào Vector Store
        try {
            ingestAllInitial();
        } catch (Exception e) {
            log.warn("Lỗi khởi tạo nạp tri thức AI ban đầu: {}", e.getMessage());
        }
    }

    private Document buildProductDocument(Product p) {
        StringBuilder sb = new StringBuilder();
        sb.append("Loại: Sản phẩm\n");
        sb.append("Tên: ").append(p.getName() != null ? p.getName() : "").append("\n");
        sb.append("Mã sản phẩm: ").append(p.getProductCode() != null ? p.getProductCode() : "").append("\n");
        sb.append("Danh mục: ").append(p.getCategories() != null && !p.getCategories().isEmpty()
                ? p.getCategories().stream().map(Category::getName).collect(Collectors.joining(", "))
                : "Không rõ").append("\n");
        sb.append("Thương hiệu: ").append(p.getBrand() != null ? p.getBrand().getName() : "Không rõ").append("\n");
        sb.append("Mô tả: ").append(p.getDescription() != null ? p.getDescription() : "").append("\n");

        // HẠNG MỤC 2: Metadata mở rộng phục vụ AI Recommendation & Semantic Search
        if (p.getTargetSport() != null && !p.getTargetSport().isBlank()) {
            sb.append("Môn thể thao: ").append(p.getTargetSport()).append("\n");
        }
        if (p.getFitnessLevel() != null && !p.getFitnessLevel().isBlank()) {
            sb.append("Trình độ: ").append(p.getFitnessLevel()).append("\n");
        }
        if (p.getGenderTarget() != null && !p.getGenderTarget().isBlank()) {
            sb.append("Dành cho: ").append(p.getGenderTarget()).append("\n");
        }
        if (p.getSuitableFor() != null && !p.getSuitableFor().isBlank()) {
            sb.append("Phù hợp cho: ").append(p.getSuitableFor()).append("\n");
        }
        if (p.getBenefits() != null && !p.getBenefits().isBlank()) {
            sb.append("Lợi ích & Tính năng nổi bật: ").append(p.getBenefits()).append("\n");
        }

        if (p.getProductVariants() != null && !p.getProductVariants().isEmpty()) {
            sb.append("Các phiên bản có sẵn:\n");
            p.getProductVariants().forEach(v ->
                    sb.append("  - Size ").append(v.getSize())
                            .append(", Màu ").append(v.getColor())
                            .append(", Giá ").append(v.getPrice()).append("đ")
                            .append(", Tồn kho: ").append(v.getStockQuantity()).append("\n")
            );
        }

        Metadata metadata = Metadata.from("type", "product")
                .put("id", p.getId().toString())
                .put("targetSport", p.getTargetSport() != null ? p.getTargetSport() : "")
                .put("fitnessLevel", p.getFitnessLevel() != null ? p.getFitnessLevel() : "")
                .put("genderTarget", p.getGenderTarget() != null ? p.getGenderTarget() : "");

        return Document.from(sb.toString(), metadata);
    }

    @Transactional
    public void ingestAllInitial() {
        List<Document> documents = new ArrayList<>();

        List<Product> products = productRepository.findAll();
        for (Product p : products) {
            documents.add(buildProductDocument(p));
        }

        List<Blog> blogs = blogRepository.findAll();
        for (Blog b : blogs) {
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
        }

        List<StorePolicy> policies = storePolicyRepository.findAll();
        for (StorePolicy sp : policies) {
            String text = String.format(
                    "Loại: Chính sách/Hỗ trợ\nTiêu đề: %s\nNhóm: %s\nNội dung:\n%s",
                    sp.getTitle() != null ? sp.getTitle() : "",
                    sp.getCategory() != null ? sp.getCategory() : "",
                    sp.getContent() != null ? sp.getContent() : "");

            Metadata metadata = Metadata.from("type", "policy").put("id", sp.getId().toString()).put("key", sp.getPolicyKey());
            documents.add(Document.from(text, metadata));
        }

        if (!documents.isEmpty()) {
            try {
                EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                        .embeddingModel(embeddingModel)
                        .embeddingStore(embeddingStore)
                        .build();

                ingestor.ingest(documents);
                log.info(">>> [DATA INGESTION] Đã nạp thành công {} tài liệu ban đầu vào kho Vector Store AI.", documents.size());
            } catch (Exception e) {
                log.warn(">>> [DATA INGESTION] Bỏ qua nạp vector ban đầu: {}", e.getMessage());
            }
        }
    }

    // Tự động chạy định kỳ mỗi 5 phút để nạp các sản phẩm mới thêm
    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void autoIngest() {
        log.info(">>> Đang kiểm tra dữ liệu mới để nạp vào kho tri thức AI...");
        List<Document> documents = new ArrayList<>();

        List<Product> newProducts = productRepository.findByIsVectorizedFalse();
        for (Product p : newProducts) {
            documents.add(buildProductDocument(p));
            p.setIsVectorized(true);
        }

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
