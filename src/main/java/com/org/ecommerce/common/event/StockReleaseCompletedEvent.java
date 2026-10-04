package com.org.ecommerce.common.event;

import com.org.ecommerce.common.enums.StockReleaseCompletionStatus;

public record StockReleaseCompletedEvent(
        Long orderId,
        StockReleaseCompletionStatus status,
        String reason
) {
}
