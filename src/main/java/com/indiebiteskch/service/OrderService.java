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

    // Create order; will reserve stock until "pay" button is clicked (demo purpose)
    // and check for inventory 
    @Transactional
    public Order reservation(OrderRequest request)
    {
        Order newOrder = new Order();
        newOrder.setStatus("PENDING");
        newOrder.setCreatedAt(LocalDateTime.now());

        List<OrderItem> orderList = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for(OrderItemRequest itemReq: request.items())
        {
            //product should exist; but precautionary check
            Product prod = productRepo.findByProductId(itemReq.productId())
                            .orElseThrow(() -> new ProductIDNotFoundException(itemReq.productId()));
            Inventory inv = prod.getInventory();

            // as some of the products (beverage) may not have inventory
            // only check if inv is not null (aka they're checked-stock); reserve them for customer
            if(inv != null)
            {
                //throw exception if requested quantity is more than available stock
                if(inv.getInStock() - inv.getReservedQty() < itemReq.quantity())
                {
                    throw new InsufficientStockException("Insufficient stock for product ID: " + prod.getProductName());
                }
                //otherwise, update reserved quantity and hold expiry
                inv.setReservedQty(inv.getReservedQty() + itemReq.quantity());
                inv.setHoldExpiresAt(LocalDateTime.now().plusMinutes(15));
            }

            //create new orderItem for each product in order
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(prod);
            orderItem.setOrderQty(itemReq.quantity());
            orderItem.setPriceAtPurchase(prod.getProductPrice());
            orderItem.setOrder(newOrder);
            //finally, add to list of this order
            orderList.add(orderItem);

            //each item price * quantity for total amount
            totalAmount = totalAmount.add(prod.getProductPrice()
                            .multiply(BigDecimal.valueOf(itemReq.quantity())));
        }
        newOrder.setItems(orderList);
        newOrder.setTotal(totalAmount);
        return orderRepo.save(newOrder);
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

        for(OrderItem item: existing.getItems())
        {
           Inventory inv = item.getProduct().getInventory();
           if(inv != null)
           {
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
