package com.org.ecommerce.idempotency.repository;

import com.org.ecommerce.idempotency.entity.ReleasedStockCompletion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReleasedStockCompletionRepository extends JpaRepository<ReleasedStockCompletion, Long> {
}
