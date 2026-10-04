package com.org.ecommerce.common.client;

public record InventoryResponse(
        Long id,
        Long productId,
        int stockQuantity
) {
}
