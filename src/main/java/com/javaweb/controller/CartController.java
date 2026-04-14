package com.javaweb.controller;

import com.javaweb.dto.AddCartRequestDTO;
import com.javaweb.dto.CartDTO;
import com.javaweb.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @GetMapping
    public ResponseEntity<CartDTO> getMyCart() {
        CartDTO cart = cartService.getCartForUser(getCurrentUserEmail());
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/items")
    public ResponseEntity<CartDTO> addToCart(@RequestBody AddCartRequestDTO request) {
        CartDTO updatedCart = cartService.addCartItem(request, getCurrentUserEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(updatedCart);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartDTO> removeCartItem(@PathVariable Long itemId) {
        CartDTO updatedCart = cartService.removeCartItem(itemId, getCurrentUserEmail());
        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart(getCurrentUserEmail());
        return ResponseEntity.noContent().build();
    }
}
