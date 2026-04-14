package com.javaweb.service;

import com.javaweb.dto.AddCartRequestDTO;
import com.javaweb.dto.CartDTO;

public interface CartService {
    CartDTO getCartForUser(String userEmail);
    CartDTO addCartItem(AddCartRequestDTO request, String userEmail);
    CartDTO removeCartItem(Long cartItemId, String userEmail);
    void clearCart(String userEmail);
}
