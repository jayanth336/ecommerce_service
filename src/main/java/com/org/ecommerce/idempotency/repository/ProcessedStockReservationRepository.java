package com.org.ecommerce.idempotency.repository;

import com.org.ecommerce.idempotency.entity.ProcessedStockReservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedStockReservationRepository extends JpaRepository<ProcessedStockReservation, Long> {
}
