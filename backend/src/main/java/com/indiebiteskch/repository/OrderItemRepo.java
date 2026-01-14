package com.indiebiteskch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.indiebiteskch.entity.OrderItem;

public interface OrderItemRepo extends JpaRepository<OrderItem, Long>{
    
}
