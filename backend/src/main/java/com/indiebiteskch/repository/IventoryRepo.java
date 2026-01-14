package com.indiebiteskch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.indiebiteskch.entity.Inventory;

public interface IventoryRepo extends JpaRepository<Inventory, Long>{
    
}
