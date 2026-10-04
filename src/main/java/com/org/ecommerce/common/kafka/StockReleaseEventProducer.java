package com.org.ecommerce.common.kafka;

import com.org.ecommerce.common.event.StockReleaseEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class StockReleaseEventProducer {
    private static final String TOPIC = "stock-release-events";
    private final KafkaTemplate<String, StockReleaseEvent> kafkaTemplate;

    public StockReleaseEventProducer(KafkaTemplate<String, StockReleaseEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishStockReleaseEvent(StockReleaseEvent stockReleaseEvent) {
        kafkaTemplate.send(
                TOPIC, // TOPIC
                stockReleaseEvent.orderId().toString(), // KEY
                stockReleaseEvent // VALUE
        );
    }
}
