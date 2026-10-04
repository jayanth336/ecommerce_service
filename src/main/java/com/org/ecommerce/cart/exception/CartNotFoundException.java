package com.org.ecommerce.cart.exception;

public class CartNotFoundException extends RuntimeException {
    public CartNotFoundException(Long id) {
        super("Cart not found for user id : " + id);
    }
}
