package com.org.ecommerce.idempotency.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class ReleasedStockCompletion {
    @Id
    private Long orderId;
    private LocalDateTime processedAt;

    public ReleasedStockCompletion(Long orderId) {
        this.orderId = orderId;
        this.processedAt = LocalDateTime.now();
    }
}
