package com.indiebiteskch.exceptions;

public class OrderItemIDNotFoundException extends RuntimeException {
    public OrderItemIDNotFoundException(String message) {
        super(message);
    }
}
