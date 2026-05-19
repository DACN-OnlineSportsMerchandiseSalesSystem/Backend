package com.javaweb.repository;

import com.javaweb.entity.Blog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {
    List<Blog> findByIsVectorizedFalse();
    Optional<Blog> findBySlug(String slug);
}
