package com.javaweb.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request body for creating or updating a Blog Post")
public class BlogRequestDTO {

    @NotBlank(message = "Slug is required")
    @Schema(description = "Friendly URL slug of the blog post", example = "top-5-giay-chay-bo-2024", requiredMode = Schema.RequiredMode.REQUIRED)
    private String slug;

    @NotBlank(message = "Title is required")
    @Schema(description = "Title of the blog post", example = "Top 5 Running Shoes in 2024", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "Category/Topic of the blog post", example = "Review")
    private String category;

    @Schema(description = "Sports discipline related to the post", example = "Running")
    private String sport;

    @Schema(description = "Author of the blog post", example = "Admin")
    private String author;

    @Schema(description = "Short summary or excerpt of the blog post", example = "Discover the top running shoes to elevate your performance.")
    private String excerpt;

    @NotBlank(message = "Content is required")
    @Schema(description = "Full HTML/Text content of the blog post", example = "Detailed reviews of Nike Air Max, Adidas Ultraboost...", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "Comma-separated list of tags", example = "running, shoes, review")
    private String tags;

    @Schema(description = "URL of the blog post header/thumbnail image", example = "https://images.unsplash.com/...jpg")
    private String imageUrl;
}
