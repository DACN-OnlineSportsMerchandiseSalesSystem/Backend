package com.javaweb.service;

import com.javaweb.dto.AddCartRequestDTO;
import com.javaweb.dto.CartDTO;
import com.javaweb.dto.CreateCartRequestDTO;
import com.javaweb.dto.OrderItemRequestDTO;
import com.javaweb.dto.UpdateCartRequestDTO;
import com.javaweb.enums.CartStatus;

import java.util.List;

public interface CartService {
    List<CartDTO> getCartsForUser(String userEmail, CartStatus status);
    CartDTO getCartById(Long cartId, String userEmail);
    CartDTO getDefaultCartForUser(String userEmail);
    CartDTO createCart(CreateCartRequestDTO request, String userEmail);
    CartDTO updateCart(Long cartId, UpdateCartRequestDTO request, String userEmail);
    void archiveCart(Long cartId, String userEmail);
    CartDTO addCartItem(AddCartRequestDTO request, String userEmail);
    CartDTO addCartItem(Long cartId, AddCartRequestDTO request, String userEmail);
    CartDTO removeCartItem(Long cartItemId, String userEmail);
    CartDTO removeCartItem(Long cartId, Long cartItemId, String userEmail);
    CartDTO updateCartItemQuantity(Long cartItemId, Integer quantity, String userEmail);
    CartDTO updateCartItemQuantity(Long cartId, Long cartItemId, Integer quantity, String userEmail);
    void clearCart(String userEmail);
    void clearCart(Long cartId, String userEmail);
    List<OrderItemRequestDTO> getCheckoutItems(Long cartId, String userEmail);
    void markCartCheckedOut(Long cartId, String userEmail);
}
