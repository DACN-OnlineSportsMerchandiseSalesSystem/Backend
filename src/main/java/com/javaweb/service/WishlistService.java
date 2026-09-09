package com.javaweb.service;

import com.javaweb.dto.ProductDTO;
import java.util.List;

public interface WishlistService {
    List<ProductDTO> getWishlist(String email);
    void addToWishlist(String email, Long productId);
    void removeFromWishlist(String email, Long productId);
    boolean isInWishlist(String email, Long productId);
}
