package com.indiebiteskch.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    private final OrderRepo orderRepo;
    private final InventoryRepo invenRepo;
    private final ProductRepo productRepo;

    @Autowired
    public OrderService(OrderRepo orderRepo, ProductRepo productRepo, InventoryRepo invenRepo) {
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
        this.invenRepo = invenRepo;
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

        // Get existing order or create a new one
        if (existingOrderId == null) {
            order = new Order();
            order.setStatus("PENDING");
            order.setTotal(BigDecimal.ZERO);
        } else {
            order = orderRepo.findById(existingOrderId)
                    .orElseThrow(() -> new OrderIDNotFoundException("Order id: " + existingOrderId + " not found"));
            
            // only allow adding to order if order is in PENDING status, else throw exception
            if (!"PENDING".equals(order.getStatus())) {
            throw new IllegalStateException("Order " + existingOrderId + " is " + order.getStatus() + ". Items can only be removed from PENDING orders.");
        }
        }

        // Logic to handle the product & inventory
        Product prod = productRepo.findByProductId(itemReq.productId())
                .orElseThrow(() -> new ProductIDNotFoundException(itemReq.productId()));

        Inventory inv = prod.getInventory();
        if (inv != null) {
            // if what's in stock - current reserved qty is less than the requested qty
            // throw insufficient stock exception
            if (inv.getInStock() - inv.getReservedQty() < itemReq.quantity()) {
                throw new InsufficientStockException("Insufficient stock for product: " + prod.getProductName());
            }

            // otherwise, set new reserved qty with current reserved + requested qty
            inv.setReservedQty(inv.getReservedQty() + itemReq.quantity());

            // RESET the timer for the entire order
            inv.setHoldExpiresAt(LocalDateTime.now().plusMinutes(15));
        }

        // Link item to order
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(prod);
        orderItem.setOrderQty(itemReq.quantity());
        orderItem.setPriceAtPurchase(prod.getProductPrice());
        order.addItem(orderItem);

        // Update total and save
        BigDecimal currentTotal = order.getTotal() != null ? order.getTotal() : BigDecimal.ZERO;
        order.setTotal(currentTotal.add(prod.getProductPrice().multiply(BigDecimal.valueOf(itemReq.quantity()))));

        return orderRepo.save(order);
    }

    // when customer pays, finalize the order and deduct stock accordingly
    @Transactional
    public Order finalizeOrder(Long orderId) {
        Order existing = getOrderById(orderId);

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
                // double check if enough stock is available
                if (inv.getInStock() < item.getOrderQty()) {
                    throw new InsufficientStockException("Insufficient stock for product: " +
                            item.getProduct().getProductName());
                }
                // deduct stock based on order quantity
                inv.setInStock(inv.getInStock() - item.getOrderQty());
                // reduce reserved quantity accordingly
                // Math.max used to prevent negative reservedQty to prevent concurrency issues
                inv.setReservedQty(Math.max(0, inv.getReservedQty() - item.getOrderQty()));
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
        Order currentOrder = getOrderById(orderId);

        // if current order is not PENDING, throw exception
        if (!"PENDING".equals(currentOrder.getStatus())) {
            throw new IllegalStateException("Order " + orderId + " is " + currentOrder.getStatus() + ". Items can only be removed from PENDING orders.");
        }

        // check if orderItem exists in the current order
        // else throw exception saying no such orderItemID exists
        OrderItem removeItem = currentOrder.getOrderList().stream()
                .filter(item -> item.getOrderItemID().equals(orderItemId))
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
