package com.org.ecommerce.common.kafka;

import com.org.ecommerce.common.event.StockReleaseCompletedEvent;
import com.org.ecommerce.order.service.OrderService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class StockReleaseCompletionEventConsumer {
    private final OrderService orderService;

    public StockReleaseCompletionEventConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(topics = "stock-release-completed-events", groupId = "ecommerce-service")
    public void consume(StockReleaseCompletedEvent completedEvent) {
        System.out.println("Stock Release Completed Event Received :  " + completedEvent);
        orderService.handleStockReleaseCompletion(completedEvent);
    }
}
