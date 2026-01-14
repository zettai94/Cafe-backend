package com.indiebiteskch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.indiebiteskch.entity.Order;

@Repository
public interface OrderRepo extends JpaRepository<Order, Long>{
    
}
