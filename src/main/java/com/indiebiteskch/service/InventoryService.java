package com.indiebiteskch.service;

import org.springframework.stereotype.Service;
import com.indiebiteskch.entity.Inventory;

@Service
public class InventoryService {
    public boolean isItemStillAvailable(Inventory inv) {
        int effectiveStock = inv.getInStock();
        if(inv.getReservedQty() > 0 && inv.isHoldExpired()) {
            return effectiveStock > 0;
        }
        return (effectiveStock - inv.getReservedQty()) > 0;
    }
}
