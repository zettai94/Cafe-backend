package com.indiebiteskch.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.indiebiteskch.entity.Inventory;
import com.indiebiteskch.entity.Order;
import com.indiebiteskch.repository.InventoryRepo;
import com.indiebiteskch.repository.OrderRepo;

import jakarta.transaction.Transactional;

@Service
public class InventoryCleanupService {
    
    @Autowired
    private InventoryRepo invenRepo;
    @Autowired
    private OrderRepo orderRepo;

    @Scheduled(fixedRate = 60000) // runs every minute
    @Transactional
    public void cleanupExpiredReservations(){
        LocalDateTime current = LocalDateTime.now();
        
        //release locks on inventory
        List<Inventory> expiredInventories = invenRepo.findByHoldExpiresAtBefore(current);
        for(Inventory inv: expiredInventories)
        {
            inv.setReservedQty(0);
            inv.setHoldExpiresAt(null);  
        }

        //cancel abondoned orders and set to "EXPIRED" status
        List<Order> abandonedOrders =  
                    orderRepo.findByStatusAndCreatedAtBefore("PENDING", current.minusMinutes(15));
        for(Order ord: abandonedOrders)
        {
            ord.setStatus("EXPIRED");
        }
    }
}
