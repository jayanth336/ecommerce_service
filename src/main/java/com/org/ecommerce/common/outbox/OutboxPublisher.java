package com.org.ecommerce.common.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.org.ecommerce.common.event.OrderCreatedEvent;
import com.org.ecommerce.common.kafka.OrderEventProducer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
public class OutboxPublisher {
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final OrderEventProducer orderEventProducer;
    private final OutboxService outboxService;

    public OutboxPublisher(OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper,
                           OrderEventProducer orderEventProducer, OutboxService outboxService) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
        this.orderEventProducer = orderEventProducer;
        this.outboxService = outboxService;
    }

    @Scheduled(fixedDelay = 5000)
    public void publish() {
        // GET THE UNPUBLISHED RECORDS
        List<OutboxEvent> outboxEvents = outboxEventRepository.findByPublished(false);

        for(OutboxEvent event : outboxEvents) {
            System.out.println("Unpublished outbox Event: " + event);
            try {
                // OBTAIN OrderCreatedEvent
                OrderCreatedEvent orderCreatedEvent = objectMapper.readValue(event.getPayload(), OrderCreatedEvent.class);

                // PUBLISH TO TOPIC
                orderEventProducer.publishOrderCreatedEvent(orderCreatedEvent).get();

                // MARK IT AS PUBLISHED - A SEPARATE TRANSACTIONAL SERVICE METHOD TO DO THAT
                outboxService.markAsPublished(event.getId());
            } catch (JsonProcessingException | ExecutionException e) {
                // IF DESERIALIZATION OR KAFKA PUBLISHING FAILS, THE CONTROL COMES HERE
                // CURRENT EVENT IS NOT MARKED AS COMPLETE
                // PUBLISHED FLAG REMAINS FALSE - IN THE NEXT SCHEDULE, THIS RECORD GETS PICKED UP AGAIN
                System.out.println("Failed to deserialize orderCreatedEvent/ publish kafka : " + event.getId());
            } catch (InterruptedException e) {
                // .get() MAKES THE PUBLISHER WAIT FOR KAFKA, AND THAT WAITING THREAD CAN BE INTERRUPTED
                // InterruptedException MEANS ANOTHER PART OF THE APPLICATION IS ASKING THE THREAD TO STOP WAITING
                // SO WE ARE SAYING IT'S OK - YOU CAN STOP THE THREAD
                // WE DON'T MARK IT AS TRUE - SO WE CAN RETRY - IDEMPOTENCY IS THERE IN CONSUMER END - SO NO WORRIES
                Thread.currentThread().interrupt();
                System.out.println("Outbox publisher interrupted");
            }
        }
    }

    /**
     * CartService: instead of directly publishing OrderCreatedEvent, save it as an OutboxEvent in the same DB transaction as the order.
     * Outbox table: stores the event as JSON with published = false.
     * OutboxPublisher: periodically reads unpublished events, converts JSON back to OrderCreatedEvent, and publishes to Kafka.
     * After Kafka success: OutboxService marks the event as published = true; if publishing fails, it stays false and is retried.
     *
     * 1. What problem does the Outbox Pattern solve in our ecommerce project?
     * - We need to save the Order in our DB and publish an OrderCreated event to Kafka.
     *   Without Outbox, the DB transaction could succeed while Kafka publishing fails.
     *   We solve this by saving the Order and OutboxEvent in the same DB transaction, then publishing the event asynchronously.
     *
     * 2. What happens if Kafka is down when the order is created?
     * - The Order and OutboxEvent are still committed successfully in the DB.
     *   Since the event remains published = false, our Outbox Publisher retries it later when Kafka becomes available.
     *   So the order event is not lost just because Kafka was temporarily unavailable.
     *
     * 3. What happens if Kafka accepts the event but our Outbox Publisher crashes before marking it as published?
     * - The event can be published to Kafka, but published remains false.
     *   When the publisher runs again, it publishes the same event again.
     *   Therefore, Outbox provides at-least-once delivery, not exactly-once delivery.
     *   Our consumer is idempotent, so processing the duplicate event doesn't cause duplicate business effects.
     *
     * 4. How exactly do we make our Inventory Consumer idempotent?
     * - We maintain an identifier for the processed event and check whether that event has already been processed before applying the business operation.
     *   If it was already processed, we skip it.
     *   This ensures that Kafka redelivery or Outbox duplicate publishing doesn't cause stock to be deducted twice.
     *
     * 5. Does the Outbox Pattern guarantee exactly-once processing?
     * - No. Our Outbox implementation provides at-least-once event publishing.
     *   An event can be published more than once because the publisher can crash between Kafka publishing and marking the event as published.
     *   That's why we combine Outbox with an idempotent consumer to achieve effectively-once business effects.
     */
}
