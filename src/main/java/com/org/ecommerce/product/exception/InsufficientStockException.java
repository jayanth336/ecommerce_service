package com.org.ecommerce.product.exception;

public class InsufficientStockException extends RuntimeException{
    public InsufficientStockException(String productName, int availableStock) {
        super("Only " + availableStock + " units of " + productName + " are available");
    }
}
