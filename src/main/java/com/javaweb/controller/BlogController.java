package com.javaweb.controller;

import com.javaweb.dto.BlogDTO;
import com.javaweb.dto.BlogRequestDTO;
import com.javaweb.service.BlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
@Tag(name = "Blog Management", description = "Endpoints for managing and querying blog posts and sports articles")
public class BlogController {

    private final BlogService blogService;

    @GetMapping
    @Operation(summary = "Retrieve all blog posts", description = "Get a list of all published blog posts in the system.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    })
    public ResponseEntity<List<BlogDTO>> getAllBlogs() {
        return ResponseEntity.ok(blogService.getAllBlogs());
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Retrieve a blog post by its slug", description = "Provide a URL slug to fetch full details of a specific blog post.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the blog post"),
        @ApiResponse(responseCode = "404", description = "Blog post not found with the given slug")
    })
    public ResponseEntity<BlogDTO> getBlogBySlug(
            @Parameter(description = "Friendly URL slug of the blog", example = "top-5-giay-chay-bo-2024", required = true)
            @PathVariable String slug) {
        return ResponseEntity.ok(blogService.getBlogBySlug(slug));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new blog post", description = "Admin only. Add a new blog post to the system. The new post will automatically be scheduled for AI vector ingestion.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Blog post created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input payload"),
        @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN authority")
    })
    public ResponseEntity<BlogDTO> createBlog(@Valid @RequestBody BlogRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(blogService.createBlog(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing blog post", description = "Admin only. Modify details of a blog post by its ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Blog post updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input payload"),
        @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN authority"),
        @ApiResponse(responseCode = "404", description = "Blog post not found with the given ID")
    })
    public ResponseEntity<BlogDTO> updateBlog(
            @Parameter(description = "ID of the blog to update", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody BlogRequestDTO request) {
        return ResponseEntity.ok(blogService.updateBlog(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a blog post", description = "Admin only. Permanently delete a blog post from the system database by its ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Blog post deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN authority"),
        @ApiResponse(responseCode = "404", description = "Blog post not found with the given ID")
    })
    public ResponseEntity<Void> deleteBlog(
            @Parameter(description = "ID of the blog to delete", example = "1", required = true)
            @PathVariable Long id) {
        blogService.deleteBlog(id);
        return ResponseEntity.noContent().build();
    }
}
