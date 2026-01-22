package com.indiebiteskch.dto;

import java.util.List;

public record OrderRequest (List<OrderItemRequest> items, String customerNote)    
{}
