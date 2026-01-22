package com.indiebiteskch.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.indiebiteskch.dto.OrderRequest;
import com.indiebiteskch.entity.Order;
import com.indiebiteskch.repository.OrderRepo;
import com.indiebiteskch.repository.ProductRepo;

import jakarta.transaction.Transactional;

@Service
public class OrderService {
    
    private final OrderRepo orderRepo;
    private final ProductRepo productRepo;

    @Autowired
    public OrderService(OrderRepo orderRepo, ProductRepo productRepo) {
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
    }

    // Create order; will check on ProductService if products exist
    // and check for inventory 
    @Transactional
    public Order createOrder(OrderRequest request)
    {
        return null;
    }
}
