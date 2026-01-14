package com.indiebiteskch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.indiebiteskch.entity.Product;
import java.util.List;
import java.util.Optional;


public interface ProductRepo extends JpaRepository<Product, Long>{
    
    Optional<Product> findByProductName(String name);
    List<Product> findAllByCategory(String category);
    List<Product> findByProductNameContainingIgnoreCase(String name);
    Optional<Product>findByProductId(Long id);
}
