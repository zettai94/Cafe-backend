package com.indiebiteskch.dto;

import java.util.List;

public record OrderRequest (List<OrderItemRequest> items)    
{ 
    // Record to hold order item requests
    // Future reference: if want to implement customerNote
}
