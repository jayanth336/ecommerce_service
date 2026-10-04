package com.org.ecommerce.common.exception;

public class InventoryServiceUnavailableException extends RuntimeException{
    public InventoryServiceUnavailableException() {
        super("Inventory service unavailable");
    }
}
