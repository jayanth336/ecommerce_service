package com.org.ecommerce.common.event;

import java.util.List;

public record OrderCreatedEvent(
        Long orderId,
        List<OrderItemEvent> orderItemEventList
) {
}
