package com.org.ecommerce.cartItem.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CartItemResponseDto {
    private Long productId;
    private String productName;
    private int quantity;
    private BigDecimal price;
}