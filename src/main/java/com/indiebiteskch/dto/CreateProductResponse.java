package com.indiebiteskch.dto;

import com.indiebiteskch.entity.Product;
import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class CreateProductResponse {
    
    private String status;
    private Product product;
}