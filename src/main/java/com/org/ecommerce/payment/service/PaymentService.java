package com.org.ecommerce.payment.service;

import com.org.ecommerce.common.enums.OrderStatus;
import com.org.ecommerce.common.enums.PaymentMethod;
import com.org.ecommerce.common.enums.PaymentStatus;
import com.org.ecommerce.common.event.OrderItemEvent;
import com.org.ecommerce.common.event.StockReleaseEvent;
import com.org.ecommerce.common.kafka.StockReleaseEventProducer;
import com.org.ecommerce.order.entity.Order;
import com.org.ecommerce.orderItem.entity.OrderItem;
import com.org.ecommerce.payment.entity.Payment;
import com.org.ecommerce.payment.gateway.DummyPaymentGateway;
import com.org.ecommerce.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final DummyPaymentGateway dummyPaymentGateway;
    private final StockReleaseEventProducer stockReleaseEventProducer;

    public PaymentService(PaymentRepository paymentRepository, DummyPaymentGateway dummyPaymentGateway,
                          StockReleaseEventProducer stockReleaseEventProducer) {
        this.paymentRepository = paymentRepository;
        this.dummyPaymentGateway = dummyPaymentGateway;
        this.stockReleaseEventProducer = stockReleaseEventProducer;
    }

    public Payment makePayment(Order order) {
        System.out.println("Order received: " + order);

        // CREATE PAYMENT OBJECT
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(PaymentMethod.UPI);
        payment.setPaymentDate(LocalDateTime.now());

        // MAKE THE PAYMENT
        PaymentStatus paymentStatus = dummyPaymentGateway.processPayment(order);

        // IF SUCCESS, THEN UPDATE THE PAYMENT AND ORDER STATUS
        if(paymentStatus == PaymentStatus.SUCCESS) {
            System.out.println("Payment created: " + payment);
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            order.setStatus(OrderStatus.PAID);
            return paymentRepository.save(payment);
        }

        // ELSE, PERFORM COMPENSATING ACTION - RELEASE RESERVED STOCK
        // CREATE THE EVENT TO CALL THE PRODUCER TO RELEASE RESERVED STOCK
        payment.setPaymentStatus(PaymentStatus.FAILED);
        order.setStatus(OrderStatus.STOCK_RELEASE_PENDING);
        Long orderId = order.getId();
        List<OrderItem> orderItems = order.getOrderItems();
        List<OrderItemEvent> orderItemEventList = orderItems.stream()
                .map(
                        orderItem -> new OrderItemEvent(orderItem.getProduct().getId(), orderItem.getQuantity())
                ).toList();

        // CALL THE STOCK_RELEASE_EVENT_PRODUCER
        StockReleaseEvent stockReleaseEvent = new StockReleaseEvent(orderId, orderItemEventList);
        stockReleaseEventProducer.publishStockReleaseEvent(stockReleaseEvent);
        return paymentRepository.save(payment);
    }
}
