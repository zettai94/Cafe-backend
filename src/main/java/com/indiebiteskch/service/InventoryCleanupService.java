package com.indiebiteskch.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.indiebiteskch.entity.Inventory;
import com.indiebiteskch.entity.Order;
import com.indiebiteskch.entity.OrderItem;
import com.indiebiteskch.repository.OrderRepo;

import jakarta.transaction.Transactional;

@Service
public class InventoryCleanupService {
    
    @Autowired
    private OrderRepo orderRepo;

    @Scheduled(fixedRate = 60000) // runs every minute
    @Transactional
    public void cleanupExpiredReservations(){
        // if time now is 2 pm, any thing before 1.45 pm (15 mins ago) is expired
        // faster cleanup than checking for every order to see how long since created
        LocalDateTime treshold = LocalDateTime.now().minusMinutes(15);
        
        //find pending order that's timed out
       List<Order> expiredOrders = 
                    orderRepo.findByStatusAndCreatedAtBefore("PENDING", treshold);
        for(Order ord: expiredOrders)
        {
            for(OrderItem item: ord.getOrderList())
            {
                Inventory inv = item.getProduct().getInventory();
                if(inv != null)
                {
                    int newReservationQty = Math.max(0, inv.getReservedQty() - item.getOrderQty());
                    inv.setReservedQty(newReservationQty);

                    //if newReservationQty is 0, clear holdExpiresAt
                    if(newReservationQty == 0)
                    {
                        inv.setHoldExpiresAt(null);
                    }
                }
            }
            //mark order as EXPIRED
            ord.setStatus("EXPIRED");
        }
    }
}
