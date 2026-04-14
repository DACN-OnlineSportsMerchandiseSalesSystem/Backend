package com.javaweb.service.impl;

import com.javaweb.dto.AddCartRequestDTO;
import com.javaweb.dto.CartDTO;
import com.javaweb.dto.CartItemDTO;
import com.javaweb.entity.Cart;
import com.javaweb.entity.CartItem;
import com.javaweb.entity.ProductVariant;
import com.javaweb.entity.User;
import com.javaweb.exception.ResouceNotFoundException;
import com.javaweb.repository.CartItemRepository;
import com.javaweb.repository.CartRepository;
import com.javaweb.repository.ProductVariantRepository;
import com.javaweb.repository.UserRepository;
import com.javaweb.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    public CartDTO getCartForUser(String userEmail) {
        Cart cart = getOrCreateCart(userEmail);
        return mapToDTO(cart);
    }

    @Override
    public CartDTO addCartItem(AddCartRequestDTO request, String userEmail) {
        Cart cart = getOrCreateCart(userEmail);

        ProductVariant variant = productVariantRepository.findById(request.getProductVariantId())
                .orElseThrow(() -> new ResouceNotFoundException("Product Variant not found with id: " + request.getProductVariantId()));

        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProductVariant() != null && item.getProductVariant().getId().equals(variant.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity().add(request.getQuantity()));
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProductVariant(variant);
            newItem.setQuantity(request.getQuantity());
            cart.getCartItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        return mapToDTO(cartRepository.save(cart));
    }

    @Override
    public CartDTO removeCartItem(Long cartItemId, String userEmail) {
        Cart cart = getOrCreateCart(userEmail);
        
        CartItem itemToRemove = cart.getCartItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResouceNotFoundException("CartItem not found with id: " + cartItemId));

        cart.getCartItems().remove(itemToRemove);
        cartItemRepository.delete(itemToRemove);

        return mapToDTO(cart);
    }

    @Override
    public void clearCart(String userEmail) {
        Cart cart = getOrCreateCart(userEmail);
        cartItemRepository.deleteAll(cart.getCartItems());
        cart.getCartItems().clear();
    }

    private Cart getOrCreateCart(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResouceNotFoundException("User not found: " + userEmail));

        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }

    private CartDTO mapToDTO(Cart cart) {
        CartDTO dto = new CartDTO();
        dto.setId(cart.getId());

        List<CartItemDTO> itemDTOs = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        if (cart.getCartItems() != null) {
            for (CartItem item : cart.getCartItems()) {
                CartItemDTO itemDTO = new CartItemDTO();
                itemDTO.setId(item.getId());
                itemDTO.setQuantity(item.getQuantity());
                
                if (item.getProductVariant() != null) {
                    itemDTO.setProductVariantId(item.getProductVariant().getId());
                    itemDTO.setUnitPrice(item.getProductVariant().getPrice());
                    itemDTO.setImageUrl("https://placehold.co/150"); // Mock Image URL
                    
                    if (item.getProductVariant().getPrice() != null && item.getQuantity() != null) {
                        BigDecimal itemTotal = item.getProductVariant().getPrice().multiply(item.getQuantity());
                        total = total.add(itemTotal);
                    }
                }
                
                itemDTOs.add(itemDTO);
            }
        }

        dto.setItems(itemDTOs);
        dto.setTotalPrice(total);

        return dto;
    }
}
