package com.javaweb.controller;

import com.javaweb.dto.AddCartRequestDTO;
import com.javaweb.dto.CartDTO;
import com.javaweb.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
@Tag(name = "Cart Management", description = "Endpoints for retrieving shopping carts, adding, updating, and clearing cart items")
public class CartController {

    private final CartService cartService;

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @GetMapping
    @Operation(summary = "Get current user's shopping cart", description = "Retrieve all items currently added to the logged-in user's shopping cart.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved shopping cart"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required")
    })
    public ResponseEntity<CartDTO> getMyCart() {
        CartDTO cart = cartService.getCartForUser(getCurrentUserEmail());
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/items")
    @Operation(summary = "Add an item to the shopping cart", description = "Insert or increment quantity of a product variant within the user's active shopping cart.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Item added to cart successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid payload or insufficient product stock")
    })
    public ResponseEntity<CartDTO> addToCart(@RequestBody AddCartRequestDTO request) {
        CartDTO updatedCart = cartService.addCartItem(request, getCurrentUserEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(updatedCart);
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update quantity of a cart item", description = "Modify the purchase quantity of a specific item within the shopping cart.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cart item updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid quantity or insufficient stock"),
        @ApiResponse(responseCode = "404", description = "Cart item not found in user's active cart")
    })
    public ResponseEntity<CartDTO> updateCartItem(
            @Parameter(description = "ID of the cart item to update", example = "1", required = true)
            @PathVariable Long itemId,
            @Parameter(description = "New quantity for the item", example = "2", required = true)
            @RequestParam Integer quantity) {
        CartDTO updatedCart = cartService.updateCartItemQuantity(itemId, quantity, getCurrentUserEmail());
        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove an item from the cart", description = "Delete a specific item from the shopping cart by its item ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cart item removed successfully"),
        @ApiResponse(responseCode = "404", description = "Cart item not found")
    })
    public ResponseEntity<CartDTO> removeCartItem(
            @Parameter(description = "ID of the cart item to remove", example = "1", required = true)
            @PathVariable Long itemId) {
        CartDTO updatedCart = cartService.removeCartItem(itemId, getCurrentUserEmail());
        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping
    @Operation(summary = "Clear the shopping cart", description = "Remove all items from the user's shopping cart permanently.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "240", description = "Cart cleared successfully")
    })
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart(getCurrentUserEmail());
        return ResponseEntity.noContent().build();
    }
}
