package com.indiebiteskch.exceptions;

public class OrderIDDoesNotExistException extends RuntimeException {
    public OrderIDDoesNotExistException(String message) {
        super(message);
    }
}
