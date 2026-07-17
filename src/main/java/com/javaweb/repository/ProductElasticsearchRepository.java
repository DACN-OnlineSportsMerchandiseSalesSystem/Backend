package com.javaweb.repository;

import com.javaweb.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductElasticsearchRepository extends ElasticsearchRepository<ProductDocument, String> {
    // ElasticsearchRepository cung cấp sẵn các thao tác CRUD cơ bản như save(), deleteById(), findById()
}
