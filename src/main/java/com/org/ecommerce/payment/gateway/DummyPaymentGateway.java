package com.org.ecommerce.payment.gateway;

import com.org.ecommerce.common.enums.PaymentStatus;
import com.org.ecommerce.order.entity.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DummyPaymentGateway {
    @Value("${payment.simulation.result}")
    private PaymentStatus paymentStatus;

    public PaymentStatus processPayment(Order order) {
        System.out.println("Payment processing order: " + order.getId());
        return paymentStatus;
    }
}
