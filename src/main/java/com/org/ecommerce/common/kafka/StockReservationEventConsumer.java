package com.org.ecommerce.common.kafka;

import com.org.ecommerce.common.event.StockReservationEvent;
import com.org.ecommerce.order.service.OrderService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class StockReservationEventConsumer {
    private final OrderService orderService;

    public StockReservationEventConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(topics = "stock-reservation-events", groupId = "ecommerce-service")
    public void consume(StockReservationEvent stockReservationEvent) {
        System.out.println("Stock reservation event received: " + stockReservationEvent);
        orderService.handleStockReservation(stockReservationEvent);
    }
}
