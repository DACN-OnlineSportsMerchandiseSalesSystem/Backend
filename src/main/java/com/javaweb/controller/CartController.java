package com.javaweb.controller;

import com.javaweb.dto.AddCartRequestDTO;
import com.javaweb.dto.CartDTO;
import com.javaweb.dto.CreateCartRequestDTO;
import com.javaweb.dto.OrderDTO;
import com.javaweb.dto.OrderRequestDTO;
import com.javaweb.dto.UpdateCartItemRequestDTO;
import com.javaweb.dto.UpdateCartRequestDTO;
import com.javaweb.enums.CartStatus;
import com.javaweb.service.CartService;
import com.javaweb.service.OrderService;
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

import java.util.List;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
@Tag(name = "Cart Management", description = "Endpoints for retrieving shopping carts, adding, updating, and clearing cart items")
public class CartController {

    private final CartService cartService;
    private final OrderService orderService;

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @GetMapping
    @Operation(summary = "Get current user's carts", description = "Retrieve all carts owned by the logged-in user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved carts"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required")
    })
    public ResponseEntity<List<CartDTO>> getMyCarts(
            @RequestParam(required = false, defaultValue = "ACTIVE") CartStatus status) {
        return ResponseEntity.ok(cartService.getCartsForUser(getCurrentUserEmail(), status));
    }

    @GetMapping("/default")
    @Operation(summary = "Get current user's default cart", description = "Retrieve or create the default active cart for the logged-in user.")
    public ResponseEntity<CartDTO> getMyDefaultCart() {
        return ResponseEntity.ok(cartService.getDefaultCartForUser(getCurrentUserEmail()));
    }

    @PostMapping
    @Operation(summary = "Create a cart", description = "Create a new active cart for the logged-in user.")
    public ResponseEntity<CartDTO> createCart(@RequestBody CreateCartRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.createCart(request, getCurrentUserEmail()));
    }

    @GetMapping("/{cartId}")
    @Operation(summary = "Get cart by ID", description = "Retrieve a cart owned by the logged-in user.")
    public ResponseEntity<CartDTO> getCart(@PathVariable Long cartId) {
        return ResponseEntity.ok(cartService.getCartById(cartId, getCurrentUserEmail()));
    }

    @PatchMapping("/{cartId}")
    @Operation(summary = "Update cart", description = "Update cart name or set it as default.")
    public ResponseEntity<CartDTO> updateCart(@PathVariable Long cartId, @RequestBody UpdateCartRequestDTO request) {
        return ResponseEntity.ok(cartService.updateCart(cartId, request, getCurrentUserEmail()));
    }

    @DeleteMapping("/{cartId}")
    @Operation(summary = "Archive cart", description = "Soft delete a cart by marking it as archived.")
    public ResponseEntity<Void> archiveCart(@PathVariable Long cartId) {
        cartService.archiveCart(cartId, getCurrentUserEmail());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/items")
    @Operation(summary = "Add an item to the default shopping cart", description = "Insert or increment quantity of a product variant within the user's default active shopping cart.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Item added to cart successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid payload or insufficient product stock")
    })
    public ResponseEntity<CartDTO> addToCart(@RequestBody AddCartRequestDTO request) {
        CartDTO updatedCart = cartService.addCartItem(request, getCurrentUserEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(updatedCart);
    }

    @PostMapping("/{cartId}/items")
    @Operation(summary = "Add an item to a cart", description = "Insert or increment quantity of a product variant within a specific active cart.")
    public ResponseEntity<CartDTO> addToCart(@PathVariable Long cartId, @RequestBody AddCartRequestDTO request) {
        CartDTO updatedCart = cartService.addCartItem(cartId, request, getCurrentUserEmail());
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
            @RequestParam(required = false) Integer quantity,
            @RequestBody(required = false) UpdateCartItemRequestDTO request) {
        CartDTO updatedCart = cartService.updateCartItemQuantity(itemId, resolveQuantity(quantity, request), getCurrentUserEmail());
        return ResponseEntity.ok(updatedCart);
    }

    @PutMapping("/{cartId}/items/{itemId}")
    @Operation(summary = "Update quantity of a cart item", description = "Modify the purchase quantity of a specific item within a specific cart.")
    public ResponseEntity<CartDTO> updateCartItem(
            @PathVariable Long cartId,
            @PathVariable Long itemId,
            @RequestParam(required = false) Integer quantity,
            @RequestBody(required = false) UpdateCartItemRequestDTO request) {
        CartDTO updatedCart = cartService.updateCartItemQuantity(cartId, itemId, resolveQuantity(quantity, request), getCurrentUserEmail());
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

    @DeleteMapping("/{cartId}/items/{itemId}")
    @Operation(summary = "Remove an item from a cart", description = "Delete a specific item from a specific cart.")
    public ResponseEntity<CartDTO> removeCartItem(
            @PathVariable Long cartId,
            @PathVariable Long itemId) {
        CartDTO updatedCart = cartService.removeCartItem(cartId, itemId, getCurrentUserEmail());
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

    @DeleteMapping("/{cartId}/items")
    @Operation(summary = "Clear a cart", description = "Remove all items from a specific active cart.")
    public ResponseEntity<Void> clearCart(@PathVariable Long cartId) {
        cartService.clearCart(cartId, getCurrentUserEmail());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{cartId}/checkout")
    @Operation(summary = "Checkout a cart", description = "Create an order from all items in a specific active cart.")
    public ResponseEntity<OrderDTO> checkoutCart(@PathVariable Long cartId, @RequestBody(required = false) OrderRequestDTO request) {
        OrderRequestDTO checkoutRequest = request != null ? request : new OrderRequestDTO();
        if (checkoutRequest.getCartId() != null && !checkoutRequest.getCartId().equals(cartId)) {
            throw new IllegalArgumentException("Cart ID in path and request body must match");
        }
        checkoutRequest.setCartId(cartId);
        OrderDTO createdOrder = orderService.createOrder(checkoutRequest, getCurrentUserEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    private Integer resolveQuantity(Integer queryQuantity, UpdateCartItemRequestDTO request) {
        if (queryQuantity != null) {
            return queryQuantity;
        }
        if (request != null && request.getQuantity() != null) {
            return request.getQuantity();
        }
        throw new IllegalArgumentException("Quantity is required");
    }
}
