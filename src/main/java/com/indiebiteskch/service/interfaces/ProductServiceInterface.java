package com.indiebiteskch.service.interfaces;

import java.util.List;
import com.indiebiteskch.entity.Product;
import com.indiebiteskch.dto.CreateProductResponse;

public interface ProductServiceInterface{
    /*  general usage:
        * get by itemID
        * get by category
        * get by name
        * view all
    */ 
    public Product getProductById(Long id);
    public List<Product> getProductsByCategory(String category);
    public List<Product> getProductsByName(String name);
    public List<Product> getAllProducts();

    /* specifically admin usage:
        * add new item
        * update item
        * delete item
    */
    public CreateProductResponse createOrCheckProduct(Product newProduct);
    public Product updateProduct(Long id, Product product);
    public void deleteProduct(Long id);
}