package com.org.ecommerce.cart.dto;

import com.org.ecommerce.cartItem.dto.CartItemResponseDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CartResponseDto {
    private Long cartId;
    private Long userId;
    private List<CartItemResponseDto> cartItems;
}
