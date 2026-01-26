package com.indiebiteskch.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

import com.indiebiteskch.service.ProductService;
import com.indiebiteskch.entity.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /* Aim for Customers' usage:
    - Get all products
    - Get product by name (list); example- "/products?name=latte"
    - Get product by category (list); example- "/products?category=beverage"
    */

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts
                    (@RequestParam(required = false) String category,
                    @RequestParam(required = false) String name) 
    {
        //Search by category; front end should define category options
        if(category != null && !category.isEmpty())
        {
            System.out.println("category is not null");
            List<Product> productsByCategory = productService.getProductsByCategory(category);
            return ResponseEntity.status(200).body(productsByCategory);
        }
        //Search by name
        if(name != null && !name.isEmpty())
        {
            System.out.println("name is not empty nor null");
            List<Product> productsByName = productService.getProductsByName(name);
            return ResponseEntity.status(200).body(productsByName);
        }
        //Otherwise, get all products
        System.out.println("DEBUG: No parameters detected. Returning ALL.");
        return ResponseEntity.status(200).body(productService.getAllProducts());
    }


}