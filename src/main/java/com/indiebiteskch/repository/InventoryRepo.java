package com.indiebiteskch.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.indiebiteskch.entity.Inventory;

public interface InventoryRepo extends JpaRepository<Inventory, Long>{
    List<Inventory> findByHoldExpiresAtBefore(LocalDateTime now);
}
