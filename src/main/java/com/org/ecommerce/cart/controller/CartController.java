package com.org.ecommerce.cart.controller;

import com.org.ecommerce.cart.dto.CartRequestDto;
import com.org.ecommerce.cart.dto.CartResponseDto;
import com.org.ecommerce.cart.service.CartService;
import com.org.ecommerce.order.dto.OrderResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@Tag(
        name = "Cart",
        description = "Shopping cart and checkout APIs"
)
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @Operation(
            summary = "Add product to cart",
            description = "Adds a product to the authenticated user's cart."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product added to cart"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PostMapping("/items")
    public ResponseEntity<CartResponseDto> addToCart(@Valid @RequestBody CartRequestDto requestDto) {
        CartResponseDto responseDto = cartService.addToCart(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(
            summary = "View cart",
            description = "Returns the authenticated user's shopping cart."
    )
    @GetMapping()
    public ResponseEntity<CartResponseDto> getCart() {
        CartResponseDto responseDto = cartService.getCart();
        return ResponseEntity.ok(responseDto);
    }

    @Operation(
            summary = "Checkout",
            description = "Creates an order from the cart and processes payment."
    )
    @PostMapping("/checkout")
    public ResponseEntity<OrderResponseDto> checkout() {
        OrderResponseDto responseDto = cartService.processOrder();
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponseDto> updateQuantity(@PathVariable Long cartItemId, @RequestParam int quantity) {
        return ResponseEntity.ok(cartService.updateQuantity(cartItemId, quantity));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeCartItem(@PathVariable Long itemId) {
        cartService.removeCartItem(itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping()
    public ResponseEntity<Void> deleteCart() {
        cartService.deleteCart();
        return ResponseEntity.noContent().build();
    }
}
