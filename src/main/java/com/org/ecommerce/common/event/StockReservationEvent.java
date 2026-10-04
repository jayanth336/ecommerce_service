package com.org.ecommerce.common.event;

import com.org.ecommerce.common.enums.StockReservationStatus;

public record StockReservationEvent(
        Long orderId,
        StockReservationStatus status,
        String reason
) {
}
