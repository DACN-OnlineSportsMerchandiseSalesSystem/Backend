package com.javaweb.service.impl;

import com.javaweb.dto.ProductDTO;
import com.javaweb.entity.Product;
import com.javaweb.entity.User;
import com.javaweb.entity.WishlistItem;
import com.javaweb.exception.ResouceNotFoundException;
import com.javaweb.repository.ProductRepository;
import com.javaweb.repository.UserRepository;
import com.javaweb.repository.WishlistItemRepository;
import com.javaweb.service.ProductService;
import com.javaweb.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WishlistServiceImpl implements WishlistService {

    private final WishlistItemRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getWishlist(String email) {
        User user = getUser(email);
        List<WishlistItem> items = wishlistRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        return items.stream()
                .map(item -> productService.mapProductToDTO(item.getProduct()))
                .collect(Collectors.toList());
    }

    @Override
    public void addToWishlist(String email, Long productId) {
        User user = getUser(email);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResouceNotFoundException("Product not found with id: " + productId));

        if (!wishlistRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            WishlistItem item = new WishlistItem();
            item.setUser(user);
            item.setProduct(product);
            wishlistRepository.save(item);
        }
    }

    @Override
    public void removeFromWishlist(String email, Long productId) {
        User user = getUser(email);
        wishlistRepository.deleteByUserIdAndProductId(user.getId(), productId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isInWishlist(String email, Long productId) {
        User user = getUser(email);
        return wishlistRepository.existsByUserIdAndProductId(user.getId(), productId);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResouceNotFoundException("User not found with email: " + email));
    }
}
