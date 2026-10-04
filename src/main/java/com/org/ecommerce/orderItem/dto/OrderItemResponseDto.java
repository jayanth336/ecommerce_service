package com.org.ecommerce.orderItem.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderItemResponseDto {
    private Long productId;
    private String productName;
    private int quantity;
    private BigDecimal priceAtPurchase;
}