package com.javaweb.service.impl;

import com.javaweb.dto.AddCartRequestDTO;
import com.javaweb.dto.CartDTO;
import com.javaweb.dto.CartItemDTO;
import com.javaweb.dto.CreateCartRequestDTO;
import com.javaweb.dto.OrderItemRequestDTO;
import com.javaweb.dto.UpdateCartRequestDTO;
import com.javaweb.entity.Cart;
import com.javaweb.entity.CartItem;
import com.javaweb.entity.Product;
import com.javaweb.entity.ProductVariant;
import com.javaweb.entity.User;
import com.javaweb.enums.CartStatus;
import com.javaweb.exception.ResouceNotFoundException;
import com.javaweb.repository.CartItemRepository;
import com.javaweb.repository.CartRepository;
import com.javaweb.repository.DiscountRepository;
import com.javaweb.repository.ProductVariantRepository;
import com.javaweb.repository.UserRepository;
import com.javaweb.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional
public class CartServiceImpl implements CartService {

    private static final String DEFAULT_CART_NAME = "Gio hang cua toi";

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;
    private final DiscountRepository discountRepository;

    @Override
    public List<CartDTO> getCartsForUser(String userEmail, CartStatus status) {
        User user = getUser(userEmail);
        CartStatus effectiveStatus = status != null ? status : CartStatus.ACTIVE;
        return cartRepository.findByUserIdAndStatusOrderByCreatedAtDesc(user.getId(), effectiveStatus).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CartDTO getCartById(Long cartId, String userEmail) {
        return mapToDTO(getCartForUser(cartId, userEmail));
    }

    @Override
    public CartDTO getDefaultCartForUser(String userEmail) {
        return mapToDTO(getOrCreateDefaultCart(userEmail));
    }

    @Override
    public CartDTO createCart(CreateCartRequestDTO request, String userEmail) {
        User user = getUser(userEmail);
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setName(normalizeCartName(request != null ? request.getName() : null));
        cart.setStatus(CartStatus.ACTIVE);

        boolean hasActiveCart = !cartRepository.findByUserIdAndStatus(user.getId(), CartStatus.ACTIVE).isEmpty();
        cart.setIsDefault(!hasActiveCart);

        return mapToDTO(cartRepository.save(cart));
    }

    @Override
    public CartDTO updateCart(Long cartId, UpdateCartRequestDTO request, String userEmail) {
        Cart cart = getActiveCartForUser(cartId, userEmail);
        if (request == null) {
            return mapToDTO(cart);
        }

        if (request.getName() != null) {
            cart.setName(normalizeCartName(request.getName()));
        }

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            setDefaultCart(cart);
        }

        return mapToDTO(cartRepository.save(cart));
    }

    @Override
    public void archiveCart(Long cartId, String userEmail) {
        Cart cart = getActiveCartForUser(cartId, userEmail);
        boolean wasDefault = Boolean.TRUE.equals(cart.getIsDefault());
        cart.setStatus(CartStatus.ARCHIVED);
        cart.setIsDefault(false);
        cartRepository.save(cart);

        if (wasDefault) {
            assignFallbackDefaultCart(cart.getUser().getId());
        }
    }

    @Override
    public CartDTO addCartItem(AddCartRequestDTO request, String userEmail) {
        Cart cart = getOrCreateDefaultCart(userEmail);
        return addCartItem(cart.getId(), request, userEmail);
    }

    @Override
    public CartDTO addCartItem(Long cartId, AddCartRequestDTO request, String userEmail) {
        Cart cart = getActiveCartForUser(cartId, userEmail);

        if (request == null || request.getProductVariantId() == null) {
            throw new IllegalArgumentException("Product variant is required");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        ProductVariant variant;
        if (request.getChildVariantIds() != null && !request.getChildVariantIds().isEmpty()) {
            ProductVariant defaultComboVariant = productVariantRepository.findById(request.getProductVariantId())
                    .orElseThrow(() -> new ResouceNotFoundException("Product Variant not found with id: " + request.getProductVariantId()));
            Product comboProduct = defaultComboVariant.getProducts();
            if (comboProduct == null || comboProduct.getIsCombo() == null || !comboProduct.getIsCombo()) {
                throw new IllegalArgumentException("Product is not a combo");
            }

            List<Long> sortedIds = request.getChildVariantIds().stream().sorted().collect(Collectors.toList());
            String varsSuffix = sortedIds.stream().map(Object::toString).collect(Collectors.joining("-"));
            String dynamicSku = "COMBO-" + comboProduct.getId() + "-VARS-" + varsSuffix;

            Optional<ProductVariant> existingDynamicVariant = productVariantRepository.findBySkuCode(dynamicSku);
            if (existingDynamicVariant.isPresent()) {
                variant = existingDynamicVariant.get();
            } else {
                List<ProductVariant> childVariants = productVariantRepository.findAllById(sortedIds);
                if (childVariants.size() != sortedIds.size()) {
                    throw new IllegalArgumentException("Some child variants were not found");
                }

                String sizes = childVariants.stream()
                        .map(v -> v.getProducts().getName() + ": " + (v.getSize() != null ? v.getSize() : "Default"))
                        .collect(Collectors.joining(" | "));
                String colors = childVariants.stream()
                        .map(v -> v.getProducts().getName() + ": " + (v.getColor() != null ? v.getColor() : "Default"))
                        .collect(Collectors.joining(" | "));

                ProductVariant newVariant = new ProductVariant();
                newVariant.setProducts(comboProduct);
                newVariant.setSkuCode(dynamicSku);
                newVariant.setSize(sizes);
                newVariant.setColor(colors);
                newVariant.setPrice(defaultComboVariant.getPrice());
                newVariant.setOriginalPrice(defaultComboVariant.getOriginalPrice());
                newVariant.setDiscount(defaultComboVariant.getDiscount());
                newVariant.setStockQuantity(0);

                variant = productVariantRepository.save(newVariant);
            }
        } else {
            variant = productVariantRepository.findById(request.getProductVariantId())
                    .orElseThrow(() -> new ResouceNotFoundException("Product Variant not found with id: " + request.getProductVariantId()));
        }

        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProductVariant() != null && item.getProductVariant().getId().equals(variant.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int currentQuantity = item.getQuantity() != null ? item.getQuantity() : 0;
            int newQuantity = currentQuantity + request.getQuantity();
            validateStock(variant, newQuantity);
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            validateStock(variant, request.getQuantity());
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
    public CartDTO updateCartItemQuantity(Long cartItemId, Integer quantity, String userEmail) {
        Cart cart = getOrCreateDefaultCart(userEmail);
        return updateCartItemQuantity(cart.getId(), cartItemId, quantity, userEmail);
    }

    @Override
    public CartDTO updateCartItemQuantity(Long cartId, Long cartItemId, Integer quantity, String userEmail) {
        Cart cart = getActiveCartForUser(cartId, userEmail);
        CartItem itemToUpdate = getCartItem(cart, cartItemId);

        if (quantity != null && quantity > 0) {
            validateStock(itemToUpdate.getProductVariant(), quantity);
            itemToUpdate.setQuantity(quantity);
            cartItemRepository.save(itemToUpdate);
        } else {
            cart.getCartItems().remove(itemToUpdate);
            cartItemRepository.delete(itemToUpdate);
        }

        return mapToDTO(cartRepository.save(cart));
    }

    @Override
    public CartDTO removeCartItem(Long cartItemId, String userEmail) {
        Cart cart = getOrCreateDefaultCart(userEmail);
        return removeCartItem(cart.getId(), cartItemId, userEmail);
    }

    @Override
    public CartDTO removeCartItem(Long cartId, Long cartItemId, String userEmail) {
        Cart cart = getActiveCartForUser(cartId, userEmail);
        CartItem itemToRemove = getCartItem(cart, cartItemId);

        cart.getCartItems().remove(itemToRemove);
        cartItemRepository.delete(itemToRemove);

        return mapToDTO(cartRepository.save(cart));
    }

    @Override
    public void clearCart(String userEmail) {
        Cart cart = getOrCreateDefaultCart(userEmail);
        clearCart(cart.getId(), userEmail);
    }

    @Override
    public void clearCart(Long cartId, String userEmail) {
        Cart cart = getActiveCartForUser(cartId, userEmail);
        cartItemRepository.deleteAll(cart.getCartItems());
        cart.getCartItems().clear();
        cartRepository.save(cart);
    }

    @Override
    public List<OrderItemRequestDTO> getCheckoutItems(Long cartId, String userEmail) {
        Cart cart = getActiveCartForUser(cartId, userEmail);
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        return cart.getCartItems().stream()
                .map(item -> {
                    OrderItemRequestDTO dto = new OrderItemRequestDTO();
                    dto.setProductVariantId(item.getProductVariant().getId());
                    dto.setQuantity(item.getQuantity());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void markCartCheckedOut(Long cartId, String userEmail) {
        Cart cart = getActiveCartForUser(cartId, userEmail);
        cart.setStatus(CartStatus.CHECKED_OUT);
        cart.setIsDefault(false);
        cartRepository.save(cart);
    }

    private User getUser(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResouceNotFoundException("User not found: " + userEmail));
    }

    private Cart getCartForUser(Long cartId, String userEmail) {
        User user = getUser(userEmail);
        return cartRepository.findByIdAndUserId(cartId, user.getId())
                .orElseThrow(() -> new ResouceNotFoundException("Cart not found with id: " + cartId));
    }

    private Cart getActiveCartForUser(Long cartId, String userEmail) {
        Cart cart = getCartForUser(cartId, userEmail);
        if (cart.getStatus() != CartStatus.ACTIVE) {
            throw new IllegalArgumentException("Cart is not active");
        }
        return cart;
    }

    private Cart getOrCreateDefaultCart(String userEmail) {
        User user = getUser(userEmail);
        return cartRepository.findFirstByUserIdAndIsDefaultTrueAndStatus(user.getId(), CartStatus.ACTIVE)
                .or(() -> cartRepository.findFirstByUserIdAndStatusOrderByCreatedAtAsc(user.getId(), CartStatus.ACTIVE)
                        .map(cart -> {
                            setDefaultCart(cart);
                            return cart;
                        }))
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setName(DEFAULT_CART_NAME);
                    newCart.setStatus(CartStatus.ACTIVE);
                    newCart.setIsDefault(true);
                    return cartRepository.save(newCart);
                });
    }

    private void setDefaultCart(Cart selectedCart) {
        User user = selectedCart.getUser();
        cartRepository.findByUserIdAndStatus(user.getId(), CartStatus.ACTIVE)
                .forEach(cart -> {
                    cart.setIsDefault(cart.getId().equals(selectedCart.getId()));
                    cartRepository.save(cart);
                });
    }

    private void assignFallbackDefaultCart(Long userId) {
        cartRepository.findFirstByUserIdAndStatusOrderByCreatedAtAsc(userId, CartStatus.ACTIVE)
                .ifPresent(this::setDefaultCart);
    }

    private void validateStock(ProductVariant variant, int requestedQuantity) {
        if (variant.getSkuCode() != null && variant.getSkuCode().contains("-VARS-")) {
            String[] parts = variant.getSkuCode().split("-VARS-");
            if (parts.length > 1) {
                String[] varIdsStr = parts[1].split("-");
                for (String varIdStr : varIdsStr) {
                    Long childVarId = Long.parseLong(varIdStr);
                    ProductVariant childVariant = productVariantRepository.findById(childVarId).orElse(null);
                    if (childVariant != null) {
                        int multiplier = 1;
                        Product comboProduct = variant.getProducts();
                        if (comboProduct != null && comboProduct.getComboItems() != null) {
                            for (com.javaweb.entity.ComboItem ci : comboProduct.getComboItems()) {
                                if (ci.getProductVariant() != null && ci.getProductVariant().getProducts() != null &&
                                    childVariant.getProducts() != null &&
                                    ci.getProductVariant().getProducts().getId().equals(childVariant.getProducts().getId())) {
                                    multiplier = ci.getQuantity();
                                    break;
                                }
                            }
                        }
                        int requiredQty = requestedQuantity * multiplier;
                        int childAvailableStock = childVariant.getStockQuantity() != null ? childVariant.getStockQuantity() : 0;
                        if (requiredQty > childAvailableStock) {
                            throw new IllegalArgumentException("Sản phẩm '" + childVariant.getProducts().getName() + "' không đủ tồn kho. Yêu cầu: "
                                    + requiredQty + ", Hiện có: " + childAvailableStock);
                        }
                    }
                }
            }
            return;
        }

        int availableStock = variant.getStockQuantity() != null ? variant.getStockQuantity() : 0;
        if (requestedQuantity > availableStock) {
            String productName = variant.getProducts() != null ? variant.getProducts().getName() : "Product";
            throw new IllegalArgumentException(productName + " does not have enough stock. Requested: "
                    + requestedQuantity + ", Available: " + availableStock);
        }
    }

    private CartItem getCartItem(Cart cart, Long cartItemId) {
        return cart.getCartItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResouceNotFoundException("CartItem not found with id: " + cartItemId));
    }

    private String normalizeCartName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return DEFAULT_CART_NAME;
        }
        return name.trim();
    }

    private CartDTO mapToDTO(Cart cart) {
        CartDTO dto = new CartDTO();
        dto.setId(cart.getId());
        dto.setName(cart.getName());
        dto.setStatus(cart.getStatus());
        dto.setIsDefault(cart.getIsDefault());
        dto.setCreatedAt(cart.getCreatedAt());
        dto.setUpdatedAt(cart.getUpdatedAt());

        List<CartItemDTO> itemDTOs = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        int totalQuantity = 0;

        if (cart.getCartItems() != null) {
            List<com.javaweb.entity.Discount> activeDiscounts = discountRepository.findAllActiveDiscounts(new java.util.Date());
            List<CartItem> sortedItems = new ArrayList<>(cart.getCartItems());
            sortedItems.sort((a, b) -> a.getId().compareTo(b.getId()));

            for (CartItem item : sortedItems) {
                CartItemDTO itemDTO = new CartItemDTO();
                itemDTO.setId(item.getId());
                itemDTO.setQuantity(item.getQuantity());
                totalQuantity += item.getQuantity() != null ? item.getQuantity() : 0;

                if (item.getProductVariant() != null) {
                    ProductVariant variant = item.getProductVariant();
                    itemDTO.setProductVariantId(variant.getId());
                    BigDecimal itemPrice = variant.getPrice(activeDiscounts);
                    itemDTO.setUnitPrice(itemPrice);
                    itemDTO.setOriginalPrice(variant.getOriginalPrice());

                    int manualDiscount = variant.getDiscount() != null ? variant.getDiscount() : 0;
                    int promoDiscount = activeDiscounts.stream().filter(d -> {
                        if (d.getScope() == com.javaweb.enums.DiscountScope.GLOBAL) return true;
                        if (d.getScope() == com.javaweb.enums.DiscountScope.BRAND && variant.getProducts() != null && variant.getProducts().getBrand() != null && variant.getProducts().getBrand().getId().equals(d.getBrand() != null ? d.getBrand().getId() : null)) return true;
                        if (d.getScope() == com.javaweb.enums.DiscountScope.CATEGORY && d.getCategory() != null && variant.getProducts() != null && variant.getProducts().getCategories() != null)
                            return variant.getProducts().getCategories().stream().anyMatch(c -> c.getId().equals(d.getCategory().getId()));
                        return false;
                    }).mapToInt(com.javaweb.entity.Discount::getDiscountPercent).max().orElse(0);
                    itemDTO.setDiscount(Math.max(manualDiscount, promoDiscount));

                    if (variant.getProducts() != null) {
                        itemDTO.setProductId(variant.getProducts().getId());
                        itemDTO.setProductName(variant.getProducts().getName());

                        if (variant.getProducts().getProductImages() != null) {
                            String thumb = variant.getProducts().getProductImages().stream()
                                    .filter(img -> img.getIsThumbnail() != null && img.getIsThumbnail())
                                    .map(img -> img.getImageUrl())
                                    .findFirst()
                                    .orElse(variant.getProducts().getProductImages().stream()
                                            .map(img -> img.getImageUrl())
                                            .findFirst().orElse("https://placehold.co/150"));
                            itemDTO.setImageUrl(thumb);
                        }
                    }

                    itemDTO.setVariantInfo(variant.getSize() + " / " + variant.getColor());

                    if (itemPrice != null && item.getQuantity() != null) {
                        BigDecimal itemTotal = itemPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                        total = total.add(itemTotal);
                    }
                }

                itemDTOs.add(itemDTO);
            }
        }

        dto.setItems(itemDTOs);
        dto.setItemCount(itemDTOs.size());
        dto.setTotalQuantity(totalQuantity);
        dto.setTotalPrice(total);

        return dto;
    }
}
