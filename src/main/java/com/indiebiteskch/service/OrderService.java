package com.indiebiteskch.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.indiebiteskch.dto.OrderItemRequest;
import com.indiebiteskch.entity.Order;
import com.indiebiteskch.repository.InventoryRepo;
import com.indiebiteskch.repository.OrderRepo;
import com.indiebiteskch.entity.Product;
import com.indiebiteskch.repository.ProductRepo;
import com.indiebiteskch.entity.OrderItem;
import com.indiebiteskch.entity.Inventory;
import com.indiebiteskch.exceptions.InsufficientStockException;
import com.indiebiteskch.exceptions.OrderIDNotFoundException;
import com.indiebiteskch.exceptions.OrderItemIDNotFoundException;
import com.indiebiteskch.exceptions.ProductIDNotFoundException;

import jakarta.transaction.Transactional;

@Service
public class OrderService {

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private InventoryRepo invenRepo;

    @Autowired
    private ProductRepo productRepo;

    public Order getOrCreateOrder(Long id) {
        if (id == null) {
            Order newOrder = new Order();
            newOrder.setStatus("PENDING");
            newOrder.setTotal(BigDecimal.ZERO);
            return orderRepo.save(newOrder);
        } else {
            return orderRepo.findById(id)
                    .orElseThrow(() -> new OrderIDNotFoundException("Order id: " + id + " not found"));
        }
    }


    // // Create order; will reserve stock until "pay" button is clicked (demo purpose)
    // // and check for inventory
    @Transactional
    public Order addToOrder(Long orderId, OrderItemRequest itemReq) {
        Order order = getOrCreateOrder(orderId);

        if(order.getStatus() != "PENDING") {
            throw new IllegalStateException("Order ID " + orderId + " is not in PENDING status");
        }

        // Logic to handle the product & inventory
        Product prod = productRepo.findByProductId(itemReq.productId())
                .orElseThrow(() -> new ProductIDNotFoundException(itemReq.productId()));

        Inventory inv = prod.getInventory();
        if (inv != null) {
            //if hold is expired, the reserved quantity will be treated as 0
            //therefore opening up the stock for new customers to grab
            int effectiveReserved = inv.isHoldExpired() ? 0 : inv.getReservedQty();
            int available = inv.getInStock() - effectiveReserved;
            
            if (available < itemReq.quantity()) {
                throw new InsufficientStockException("Insufficient stock for product: " + prod.getProductName());
            }
        
            //if old reserved qty is expired, reset count before adding the new qty
            if(inv.isHoldExpired()) {
                inv.setReservedQty(itemReq.quantity());
            }
            else{
                inv.setReservedQty(inv.getReservedQty() + itemReq.quantity());
            }

            inv.setHoldExpiresAt(LocalDateTime.now().plusMinutes(15));
            invenRepo.save(inv);
        }

        // Link item to order
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(prod);
        orderItem.setOrderQty(itemReq.quantity());
        orderItem.setPriceAtPurchase(prod.getProductPrice());
        orderItem.setOrder(order);

        order.addItem(orderItem);

        // Update total and save
        BigDecimal currentTotal = order.getTotal() != null ? order.getTotal() : BigDecimal.ZERO;
        order.setTotal(currentTotal.add(prod.getProductPrice().multiply(BigDecimal.valueOf(itemReq.quantity()))));

        return orderRepo.save(order);
    }

    // when customer pays, finalize the order and deduct stock accordingly
    @Transactional
    public Order finalizeOrder(Long orderId) {
        Order existing = orderRepo.findById(orderId)
                .orElseThrow(() -> new OrderIDNotFoundException("Order id: " + orderId + " not found"));

        // Only allow PENDING status order to proceed with payment
        if (!"PENDING".equals(existing.getStatus())) {
            // may change to "DROPPED" status later
            // update "DROPPED" to be implemented in scheduler
            throw new IllegalStateException("Order ID " + orderId + " is not in PENDING status");
        }

        for (OrderItem item : existing.getOrderList()) {
            Inventory inv = item.getProduct().getInventory();
            //if tracked inventory exits
            if (inv != null) {
                
                if (inv.getInStock() < item.getOrderQty()) {
                    throw new InsufficientStockException("Insufficient stock for product: " +
                            item.getProduct().getProductName());
                }
                // deduct stock based on order quantity
                inv.setInStock(inv.getInStock() - item.getOrderQty());
                
                //refactor: if hold expired, cleanup or another user might already 
                // reset the reservedqty; make sure not less than 0
                int newReservedQty = Math.max(0, inv.getReservedQty() - item.getOrderQty());
                inv.setReservedQty(newReservedQty);

                // clear hold expiry, only if time is at 0
                if (inv.getReservedQty() == 0) {
                    /*
                     * A reserves then buys all stock before expiry
                     * B reserves some stock but doesn't buy before expiry
                     * A will cause reservedQty to drop from 2 to 1
                     * if statement prevents B from clearing holdExpiresAt
                     * and causing no timed release
                     */
                    inv.setHoldExpiresAt(null);
                }
                // Save inven accordingly; otherwise inven may not reflect beyond first in the orderitems
                invenRepo.save(inv);
            }
        }
        existing.setStatus("PAID");
        return orderRepo.save(existing);
    }

    // remove item from an order
    @Transactional 
    public Order removeItem(Long orderId, Long orderItemId)
    {
        Order currentOrder = orderRepo.findById(orderId)
                .orElseThrow(() -> new OrderIDNotFoundException("Order id: " + orderId + " not found"));

        // if current order is not PENDING, throw exception
        if (!"PENDING".equals(currentOrder.getStatus())) {
            throw new IllegalStateException("Order " + orderId + " is " + currentOrder.getStatus() + ". Items can only be removed from PENDING orders.");
        }

        // check if orderItem exists in the current order
        // else throw exception saying no such orderItemID exists
        OrderItem removeItem = currentOrder.getOrderList().stream()
                .filter(item -> item.getOrderItemId().equals(orderItemId))
                .findFirst().orElseThrow(() -> new OrderItemIDNotFoundException("OrderItemID " + orderItemId + " not found."));

        // get the inventory of that product to adjust reserved qty
        // make sure it is not null (not tracked), and the get the orderItemId's reserved qty
        Inventory inv = removeItem.getProduct().getInventory();
        if(inv != null)
        {
            int newReservationQty = Math.max(0, inv.getReservedQty() - removeItem.getOrderQty());
            inv.setReservedQty(newReservationQty);

            //remove the reservation hold if reservation is now 0
            if(newReservationQty == 0)
            {
                inv.setHoldExpiresAt(null);
            }
        }

        // update Total price accordingly
        BigDecimal deductRemovedPrice = removeItem.getPriceAtPurchase()
                .multiply(BigDecimal.valueOf(removeItem.getOrderQty()));
        currentOrder.setTotal(currentOrder.getTotal().subtract(deductRemovedPrice));

        //remove the item from list of order items
        currentOrder.getOrderList().remove(removeItem);

        //if orderItem list is empty after removal, return null (aka delete order)
        if(currentOrder.getOrderList().isEmpty())
        {
            orderRepo.delete(currentOrder);
            return null;
        }

        return orderRepo.save(currentOrder);
    }
}
