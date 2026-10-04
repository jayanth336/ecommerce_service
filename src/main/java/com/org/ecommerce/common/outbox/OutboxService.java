package com.org.ecommerce.common.outbox;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class OutboxService {
    private final OutboxEventRepository outboxEventRepository;

    public OutboxService(OutboxEventRepository outboxEventRepository) {
        this.outboxEventRepository = outboxEventRepository;
    }

    @Transactional
    public void markAsPublished(Long eventId) {
        OutboxEvent outboxEvent = outboxEventRepository.findById(eventId).orElseThrow();
        outboxEvent.setPublished(true);
    }
}
