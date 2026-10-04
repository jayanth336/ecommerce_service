package com.org.ecommerce.cart.exception;

public class InvalidQuantityException extends RuntimeException {
    public InvalidQuantityException() {
        super("Quantity must be greater than 0");
    }
}
