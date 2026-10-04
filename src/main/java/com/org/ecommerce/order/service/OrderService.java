package com.org.ecommerce.order.service;

import com.org.ecommerce.common.enums.OrderStatus;
import com.org.ecommerce.common.enums.StockReleaseCompletionStatus;
import com.org.ecommerce.common.enums.StockReservationStatus;
import com.org.ecommerce.common.event.StockReleaseCompletedEvent;
import com.org.ecommerce.common.event.StockReservationEvent;
import com.org.ecommerce.idempotency.entity.ProcessedStockReservation;
import com.org.ecommerce.idempotency.entity.ReleasedStockCompletion;
import com.org.ecommerce.idempotency.repository.ProcessedStockReservationRepository;
import com.org.ecommerce.idempotency.repository.ReleasedStockCompletionRepository;
import com.org.ecommerce.order.entity.Order;
import com.org.ecommerce.order.exception.OrderNotFoundException;
import com.org.ecommerce.order.repository.OrderRepository;
import com.org.ecommerce.payment.service.PaymentService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;
    private final ProcessedStockReservationRepository processedStockReservationRepository;
    private final ReleasedStockCompletionRepository releasedStockCompletionRepository;

    public OrderService(OrderRepository orderRepository, PaymentService paymentService, ProcessedStockReservationRepository processedStockReservationRepository, ReleasedStockCompletionRepository releasedStockCompletionRepository) {
        this.orderRepository = orderRepository;
        this.paymentService = paymentService;
        this.processedStockReservationRepository = processedStockReservationRepository;
        this.releasedStockCompletionRepository = releasedStockCompletionRepository;
    }

    @Transactional
    public void handleStockReservation(StockReservationEvent stockReservationEvent) {
        Long orderId = stockReservationEvent.orderId();

        // CHECK IF THE EVENT IS ALREADY PROCESSED - IDEMPOTENCY
        if(processedStockReservationRepository.existsById(orderId)) {
            return;
        }

        // IF NOT, THEN IT MEANS WE HAVEN'T PROCESSED IT YET
        // SO PROCESS IT NOW
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(stockReservationEvent.orderId()));

        if(stockReservationEvent.status() == StockReservationStatus.SUCCESS) {
            order.setStatus(OrderStatus.STOCK_RESERVED);
            paymentService.makePayment(order);
        } else {
            order.setStatus(OrderStatus.STOCK_RESERVATION_FAILED);
        }

        // SAVE ONLY ProcessedStockReservation ENTITY EXPLICITLY.
        // BUT NOT ORDER - BECAUSE WE HAVE @TRANSACTIONAL. SO, DIRTY CHECKING TAKES CARE OF IT.
        ProcessedStockReservation processedStockReservation = new ProcessedStockReservation(orderId);
        processedStockReservationRepository.save(processedStockReservation);
    }

    @Transactional
    public void handleStockReleaseCompletion(StockReleaseCompletedEvent completedEvent) {
        Long orderId = completedEvent.orderId();

        // CHECK IF THE EVENT IS ALREADY PROCESSED - IDEMPOTENCY
        if(releasedStockCompletionRepository.existsById(orderId)) {
            return;
        }

        // IF NOT, THEN IT MEANS WE HAVEN'T PROCESSED IT YET
        // SO PROCESS IT NOW
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // MARK THE EVENT AS PROCESSED ONLY IF STOCK RELEASE IS SUCCESSFUL.
        // IF RELEASE FAILED, DON'T MARK IT PROCESSED SO THE EVENT CAN BE RETRIED.
        if(completedEvent.status() == StockReleaseCompletionStatus.STOCK_RELEASE_COMPLETED) {
            order.setStatus(OrderStatus.FAILED);
            // SAVE ONLY ReleasedStockCompletion ENTITY EXPLICITLY.
            // BUT NOT ORDER - BECAUSE WE HAVE @TRANSACTIONAL. SO, DIRTY CHECKING TAKES CARE OF IT.
            ReleasedStockCompletion releasedStockCompletion = new ReleasedStockCompletion(orderId);
            releasedStockCompletionRepository.save(releasedStockCompletion);
        } else {
            order.setStatus(OrderStatus.STOCK_RELEASE_FAILED);
        }
    }
}
