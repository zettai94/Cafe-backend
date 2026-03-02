package com.indiebiteskch.dto;

public record OrderItemRequest (Long orderId, Long productId, int quantity)    
{}