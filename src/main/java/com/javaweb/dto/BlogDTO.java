package com.javaweb.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Data Transfer Object representing a Blog Post")
public class BlogDTO {

    @Schema(description = "Unique identifier of the blog post", example = "1")
    private Long id;

    @Schema(description = "Friendly URL slug of the blog post", example = "top-5-giay-chay-bo-2024")
    private String slug;

    @Schema(description = "Title of the blog post", example = "Top 5 Running Shoes in 2024")
    private String title;

    @Schema(description = "Category/Topic of the blog post", example = "Review")
    private String category;

    @Schema(description = "Sports discipline related to the post", example = "Running")
    private String sport;

    @Schema(description = "Author of the blog post", example = "Admin")
    private String author;

    @Schema(description = "Short summary or excerpt of the blog post", example = "Discover the top running shoes to elevate your performance.")
    private String excerpt;

    @Schema(description = "Full HTML/Text content of the blog post", example = "Detailed reviews of Nike Air Max, Adidas Ultraboost...")
    private String content;

    @Schema(description = "Comma-separated list of tags", example = "running, shoes, review")
    private String tags;

    @Schema(description = "URL of the blog post header/thumbnail image", example = "https://images.unsplash.com/...jpg")
    private String imageUrl;
}
