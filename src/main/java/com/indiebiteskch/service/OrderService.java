package com.indiebiteskch.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.indiebiteskch.dto.OrderItemRequest;
import com.indiebiteskch.dto.OrderRequest;
import com.indiebiteskch.entity.Order;
import com.indiebiteskch.repository.OrderRepo;
import com.indiebiteskch.entity.Product;
import com.indiebiteskch.repository.ProductRepo;
import com.indiebiteskch.entity.OrderItem;
import com.indiebiteskch.entity.Inventory;
import com.indiebiteskch.exceptions.InsufficientStockException;
import com.indiebiteskch.exceptions.OrderIDNotFoundException;
import com.indiebiteskch.exceptions.ProductIDNotFoundException;

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

    // Get order by ID
    public Order getOrderById(Long id) {
        return orderRepo.findById(id)
                .orElseThrow(() -> new OrderIDNotFoundException("Order id: " + id + " not found"));
    }

    // Create order; will reserve stock until "pay" button is clicked (demo purpose)
    // and check for inventory 
    @Transactional
    public Order addToOrder(Long existingOrderId, OrderItemRequest itemReq) {
        Order order;

        // 1. Get existing order or create a new one
        if (existingOrderId == null) {
            order = new Order();
            order.setStatus("PENDING");
        } else {
            order = orderRepo.findById(existingOrderId)
                    .orElseThrow(() -> new OrderIDNotFoundException("Order id: " +existingOrderId+ " not found"));
        }

        // 2. Logic to handle the product & inventory
        Product prod = productRepo.findByProductId(itemReq.productID())
                .orElseThrow(() -> new ProductIDNotFoundException(itemReq.productID()));
        
        Inventory inv = prod.getInventory();
        if (inv != null) {
            // Check stock and update reservedQty (same as your current logic)
            inv.setReservedQty(inv.getReservedQty() + itemReq.quantity());
            
            // 3. RESET the timer for the entire order
            inv.setHoldExpiresAt(LocalDateTime.now().plusMinutes(15));
        }

        // 4. Link item to order
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(prod);
        orderItem.setOrderQty(itemReq.quantity());
        orderItem.setPriceAtPurchase(prod.getProductPrice());
        order.addItem(orderItem);

        // 5. Update total and save
        BigDecimal currentTotal = order.getTotal() != null ? order.getTotal() : BigDecimal.ZERO;
        order.setTotal(currentTotal.add(prod.getProductPrice().multiply(BigDecimal.valueOf(itemReq.quantity()))));

        return orderRepo.save(order);
    }

    // when customer pays, finalize the order and deduct stock accordingly
    @Transactional
    public Order finalizeOrder(Long orderId)
    {
        Order existing = orderRepo.findById(orderId)
                        .orElseThrow(()-> new OrderIDNotFoundException("Order ID " + orderId + " not found"));

        if(!"PENDING".equals(existing.getStatus()))
        {
            // may change to "DROPPED" status later
            // update "DROPPED" to be implemented in scheduler 
            throw new IllegalStateException("Order ID " + orderId + " is not in PENDING status");
        }

        for(OrderItem item: existing.getOrderList())
        {
           Inventory inv = item.getProduct().getInventory();
           if(inv != null)
           {
                //double check if enough stock is available
                if(inv.getInStock() < item.getOrderQty())
                {
                    throw new InsufficientStockException("Insufficient stock for product: " + 
                                                        item.getProduct().getProductName());
                }
                //deduct stock based on order quantity
                inv.setInStock(inv.getInStock() - item.getOrderQty());
                //reduce reserved quantity accordingly
                //Math.max used to prevent negative reservedQty to prevent concurrency issues
                inv.setReservedQty(Math.max(0, inv.getReservedQty() - item.getOrderQty()));
                //clear hold expiry, only if time is at 0
                if(inv.getReservedQty() == 0)
                {
                    /*  A reserves then buys all stock before expiry
                        B reserves some stock but doesn't buy before expiry
                        A will cause reservedQty to drop from 2 to 1
                        if statement prevents B from clearing holdExpiresAt 
                        and causing no timed release
                     */
                    inv.setHoldExpiresAt(null);
                }
           }
        }
        existing.setStatus("PAID");
        return orderRepo.save(existing);
    }
}
