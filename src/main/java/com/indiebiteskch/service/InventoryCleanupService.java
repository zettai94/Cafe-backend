package com.indiebiteskch.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.indiebiteskch.repository.InventoryRepo;


import jakarta.transaction.Transactional;

@Service
public class InventoryCleanupService {
    
    @Autowired
    private InventoryRepo invenRepo;

    @Scheduled(fixedRate = 60000) // runs every minute
    @Transactional
    public void releaseExpiredReservations(){
        invenRepo.releaseExpiredHolds(java.time.LocalDateTime.now());
    }
}
