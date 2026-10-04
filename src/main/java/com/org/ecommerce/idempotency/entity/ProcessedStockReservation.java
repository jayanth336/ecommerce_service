package com.org.ecommerce.idempotency.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class ProcessedStockReservation {
    @Id
    private Long orderId;
    private LocalDateTime processedAt;

    public ProcessedStockReservation(Long orderId) {
        this.orderId = orderId;
        this.processedAt = LocalDateTime.now();
    }
}
