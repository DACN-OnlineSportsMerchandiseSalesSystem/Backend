package com.javaweb.controller;

import com.javaweb.repository.ProductRepository;
import com.javaweb.repository.BlogRepository;
import com.javaweb.repository.StorePolicyRepository;
import com.javaweb.entity.Product;
import com.javaweb.entity.Blog;
import com.javaweb.entity.StorePolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class TempEmbedController {
    @Autowired private ProductRepository productRepository;
    @Autowired private BlogRepository blogRepository;
    @Autowired private StorePolicyRepository storePolicyRepository;

    @GetMapping("/api/temp/reset-vector")
    public String resetVector() {
        List<Product> products = productRepository.findAll();
        for (Product p : products) { p.setIsVectorized(false); }
        productRepository.saveAll(products);

        List<Blog> blogs = blogRepository.findAll();
        for (Blog b : blogs) { b.setIsVectorized(false); }
        blogRepository.saveAll(blogs);

        List<StorePolicy> policies = storePolicyRepository.findAll();
        for (StorePolicy p : policies) { p.setIsVectorized(false); }
        storePolicyRepository.saveAll(policies);

        return "OK! All items are marked for re-embedding into the fresh Vector DB.";
    }
}