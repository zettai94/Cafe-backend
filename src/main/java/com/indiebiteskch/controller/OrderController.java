package com.indiebiteskch.controller;

import com.indiebiteskch.service.OrderService;
import com.indiebiteskch.dto.OrderItemRequest;
import com.indiebiteskch.entity.Order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    private OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    //when user proceed to payment
    @PostMapping("/add-items")
    public ResponseEntity<Order> createPendingOrder(
            @RequestParam(required = false) Long existingOrderId,
            @RequestBody OrderItemRequest orderRequest) {
        Order updatedOrder = orderService.addToOrder(existingOrderId, orderRequest);
        return ResponseEntity.ok(updatedOrder);
    }

    //get order by id
    @GetMapping("{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    //when user clicks "pay" button
    @PostMapping("{id}/pay")
    public ResponseEntity<Order> payOrder(@PathVariable Long id) {
        Order paidOrder = orderService.finalizeOrder(id);
        return ResponseEntity.ok(paidOrder);
    }


    //admin usage later: get all orders
}
