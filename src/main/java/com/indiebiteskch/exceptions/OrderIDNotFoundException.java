package com.indiebiteskch.exceptions;

public class OrderIDNotFoundException extends RuntimeException {
    public OrderIDNotFoundException(String message) {
        super(message);
    }
}
