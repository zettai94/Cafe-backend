package com.indiebiteskch.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.indiebiteskch.entity.Inventory;

public interface InventoryRepo extends JpaRepository<Inventory, Long>{
    @Modifying
    @Query("UPDATE Inventory i SET i.reservedQty = 0, i.holdExpiresAt = null WHERE i.holdExpiresAt <= :now")
    public void releaseExpiredHolds(@Param("now") LocalDateTime now);
}
