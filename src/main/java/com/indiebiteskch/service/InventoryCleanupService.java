package com.indiebiteskch.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.indiebiteskch.entity.Inventory;

import com.indiebiteskch.repository.InventoryRepo;

import jakarta.transaction.Transactional;

@Service
public class InventoryCleanupService {
    
    @Autowired
    private InventoryRepo inventoryRepo;

    @Scheduled(fixedRate = 60000) // runs every minute
    @Transactional
    public void releaseExpiredHolds() {
        LocalDateTime now = LocalDateTime.now();

        List<Inventory> expiredInventories = inventoryRepo.findByHoldExpiresAtBefore(now);
        if(expiredInventories.isEmpty()) {
            return;
        }
        
        for (Inventory inv : expiredInventories) {
            inv.setReservedQty(0);
            inv.setHoldExpiresAt(null);
        }
    }
}
