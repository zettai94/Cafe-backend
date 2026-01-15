package com.indiebiteskch.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

import com.indiebiteskch.service.ProductService;
import com.indiebiteskch.entity.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    @Autowired
    private ProductService prodService;

    @GetMapping
    public List<Product> getAll() {
        return prodService.getAllProducts();
    }
}
