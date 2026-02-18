package com.indiebiteskch.controller;

import com.indiebiteskch.service.OrderService;
import com.indiebiteskch.dto.OrderItemRequest;
import com.indiebiteskch.entity.Order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    // create new order; 
    // this is to be used when cart is clicked on and there is yet an id
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

    //remove item from order (removing item entirely from order)
    @DeleteMapping("/{orderId}/items/{orderItemId}")
    public ResponseEntity<?> deleteItem(@PathVariable Long orderId,
                                            @PathVariable Long orderItemId) {
        Order updatedOrder = orderService.removeItem(orderId, orderItemId);
        if(updatedOrder == null) {
            return ResponseEntity.ok("Order was empty and has been deleted.");
        }
        return ResponseEntity.ok(updatedOrder);
    }



    //admin usage later: get all orders
}
