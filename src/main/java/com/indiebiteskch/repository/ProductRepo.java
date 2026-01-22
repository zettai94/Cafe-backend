package com.indiebiteskch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.indiebiteskch.entity.Product;
import com.indiebiteskch.model.Category;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepo extends JpaRepository<Product, Long>{
    
    Optional<Product> findByProductName(String name);
    List<Product> findAllByCategory(Category category);
    List<Product> findByProductNameContainingIgnoreCase(String name);
    Optional<Product>findByProductId(Long id);

    @Query("SELECT p FROM Product p WHERE p.inventory.holdExpiresAt <= :now")
    List<Product> findExpiredReservations(@Param("now") LocalDateTime now);
}
