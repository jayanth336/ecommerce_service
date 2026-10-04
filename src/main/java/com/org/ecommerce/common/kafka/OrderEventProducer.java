package com.org.ecommerce.common.kafka;

import com.org.ecommerce.common.event.OrderCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class OrderEventProducer {
    private static final String TOPIC = "order-events";
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public CompletableFuture<SendResult<String, OrderCreatedEvent>> publishOrderCreatedEvent(OrderCreatedEvent orderCreatedEvent) {
        // KEY IS ORDER ID - IT TELLS THAT MESSAGES BELONGING TO SAME ORDER ID GO TO THE SAME KAFKA PARTITION
        return kafkaTemplate.send(
                TOPIC, // TOPIC
                orderCreatedEvent.orderId().toString(), // KEY
                orderCreatedEvent // VALUE
        );
    }
}
