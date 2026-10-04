package com.org.ecommerce.cart.exception;

public class EmptyCartException extends RuntimeException {
    public EmptyCartException(Long userId) {
        super("Cart is empty for user ID : " + userId);
    }
}
