package com.org.ecommerce.common.event;

public record OrderItemEvent(
        Long productId,
        int quantity
) {
}
