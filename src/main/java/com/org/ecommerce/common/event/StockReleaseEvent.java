package com.org.ecommerce.common.event;

import java.util.List;

public record StockReleaseEvent(
        Long orderId,
        List<OrderItemEvent> orderItemEventList
) {
}
