package com.javaweb.controller;

import com.javaweb.dto.ProductDTO;
import com.javaweb.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist Management", description = "Endpoints for managing user favorite products / wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @GetMapping
    @Operation(summary = "Get user wishlist", description = "Retrieve list of all favorite products of the logged-in user.")
    public ResponseEntity<List<ProductDTO>> getWishlist() {
        return ResponseEntity.ok(wishlistService.getWishlist(getCurrentUserEmail()));
    }

    @PostMapping("/{productId}")
    @Operation(summary = "Add to wishlist", description = "Add a product to the current user's favorite list.")
    public ResponseEntity<Void> addToWishlist(@PathVariable Long productId) {
        wishlistService.addToWishlist(getCurrentUserEmail(), productId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Remove from wishlist", description = "Remove a product from the current user's favorite list.")
    public ResponseEntity<Void> removeFromWishlist(@PathVariable Long productId) {
        wishlistService.removeFromWishlist(getCurrentUserEmail(), productId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{productId}/check")
    @Operation(summary = "Check if in wishlist", description = "Check if a product is in the current user's favorite list.")
    public ResponseEntity<Boolean> isInWishlist(@PathVariable Long productId) {
        return ResponseEntity.ok(wishlistService.isInWishlist(getCurrentUserEmail(), productId));
    }
}
