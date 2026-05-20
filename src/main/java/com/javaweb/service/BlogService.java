package com.javaweb.service;

import com.javaweb.dto.BlogDTO;
import com.javaweb.dto.BlogRequestDTO;
import java.util.List;

public interface BlogService {
    List<BlogDTO> getAllBlogs();
    
    BlogDTO getBlogBySlug(String slug);
    
    BlogDTO createBlog(BlogRequestDTO request);
    
    BlogDTO updateBlog(Long id, BlogRequestDTO request);
    
    void deleteBlog(Long id);
}
