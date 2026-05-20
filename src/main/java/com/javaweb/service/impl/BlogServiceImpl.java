package com.javaweb.service.impl;

import com.javaweb.dto.BlogDTO;
import com.javaweb.dto.BlogRequestDTO;
import com.javaweb.entity.Blog;
import com.javaweb.exception.ResouceNotFoundException;
import com.javaweb.repository.BlogRepository;
import com.javaweb.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlogServiceImpl implements BlogService {

    private final BlogRepository blogRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BlogDTO> getAllBlogs() {
        List<Blog> blogs = blogRepository.findAll();
        return blogs.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BlogDTO getBlogBySlug(String slug) {
        Blog blog = blogRepository.findBySlug(slug)
                .orElseThrow(() -> new ResouceNotFoundException("Blog not found with slug: " + slug));
        return mapToDTO(blog);
    }

    @Override
    @Transactional
    public BlogDTO createBlog(BlogRequestDTO request) {
        Blog blog = new Blog();
        mapToEntity(request, blog);
        blog.setIsVectorized(false); // Make sure it's vectorized by the scheduled job/CDC
        
        Blog savedBlog = blogRepository.save(blog);
        return mapToDTO(savedBlog);
    }

    @Override
    @Transactional
    public BlogDTO updateBlog(Long id, BlogRequestDTO request) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResouceNotFoundException("Blog not found with id: " + id));
        
        mapToEntity(request, blog);
        blog.setIsVectorized(false); // Reset vectorized flag so the AI store is updated
        
        Blog updatedBlog = blogRepository.save(blog);
        return mapToDTO(updatedBlog);
    }

    @Override
    @Transactional
    public void deleteBlog(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResouceNotFoundException("Blog not found with id: " + id));
        blogRepository.delete(blog);
    }

    private BlogDTO mapToDTO(Blog blog) {
        BlogDTO dto = new BlogDTO();
        dto.setId(blog.getId());
        dto.setSlug(blog.getSlug());
        dto.setTitle(blog.getTitle());
        dto.setCategory(blog.getCategory());
        dto.setSport(blog.getSport());
        dto.setAuthor(blog.getAuthor());
        dto.setExcerpt(blog.getExcerpt());
        dto.setContent(blog.getContent());
        dto.setTags(blog.getTags());
        dto.setImageUrl(blog.getImageUrl());
        return dto;
    }

    private void mapToEntity(BlogRequestDTO request, Blog blog) {
        blog.setSlug(request.getSlug());
        blog.setTitle(request.getTitle());
        blog.setCategory(request.getCategory());
        blog.setSport(request.getSport());
        blog.setAuthor(request.getAuthor());
        blog.setExcerpt(request.getExcerpt());
        blog.setContent(request.getContent());
        blog.setTags(request.getTags());
        blog.setImageUrl(request.getImageUrl());
    }
}
