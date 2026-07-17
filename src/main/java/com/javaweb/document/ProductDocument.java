package com.javaweb.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Document(indexName = "products")
@Getter
@Setter
public class ProductDocument {

    @Id
    private String id; // Ánh xạ từ ID sản phẩm (dạng String)

    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;

    @Field(type = FieldType.Keyword)
    private String productCode;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String searchTag;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Keyword)
    private String slug;

    @Field(type = FieldType.Keyword)
    private String brandName;

    @Field(type = FieldType.Keyword)
    private List<String> categories;

    @Field(type = FieldType.Keyword)
    private List<String> colors;

    @Field(type = FieldType.Keyword)
    private List<String> sizes;

    @Field(type = FieldType.Double)
    private Double price; // Giá bán nhỏ nhất từ các biến thể
}
