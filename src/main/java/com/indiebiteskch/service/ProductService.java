package com.indiebiteskch.service;

import com.indiebiteskch.dto.CreateProductResponse;
import com.indiebiteskch.entity.Product;
import com.indiebiteskch.repository.ProductRepo;
import com.indiebiteskch.service.interfaces.ProductServiceInterface;

import jakarta.transaction.Transactional;

import com.indiebiteskch.exceptions.InsufficientStockException;
import com.indiebiteskch.exceptions.ProductIDNotFoundException;
import com.indiebiteskch.model.Category;
import com.indiebiteskch.entity.Inventory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class ProductService implements ProductServiceInterface{

    private final ProductRepo productRepo;

    @Autowired
    public ProductService(ProductRepo productRepo) {
        this.productRepo = productRepo;
    }

    @Override
    public Product getProductById(Long id)
    {
        return productRepo.findByProductId(id).orElseThrow(() -> new ProductIDNotFoundException(id));
    }

    @Override
    public List<Product> getProductsByCategory(String category)
    {
        Category cat = Category.valueOf(category.toUpperCase());
        return productRepo.findAllByCategory(cat);
    }

    //search bar usage; get all product containing certain string
    @Override
    public List<Product> getProductsByName(String name)
    {
        return productRepo.findByProductNameContainingIgnoreCase(name);
    }

    @Override
    public List<Product> getAllProducts()
    {
        return productRepo.findAll();
    }

    @Override
    public CreateProductResponse createOrCheckProduct(Product newProduct)
    {
        Optional<Product> existing = productRepo.findByProductName(newProduct.getProductName());
        if(existing.isPresent())
        {
            return new CreateProductResponse("EXIST", existing.get());
            //to be handled in Controller 
        }
        //otherwise create new product
        Product saved = productRepo.save(newProduct);
        return new CreateProductResponse("CREATED", saved);
    }

    @Override
    public Product updateProduct(Long id, Product product)
    {
        Product existing = productRepo.findByProductId(id)
                            .orElseThrow(()-> new ProductIDNotFoundException(id));
        
        //changes only account for category, product name, price, and URL                    
        existing.setCategory(product.getCategory());
        existing.setProductName(product.getProductName());
        existing.setProductPrice(product.getProductPrice());
        existing.setDescription(product.getDescription());
        existing.setProductImageURL(product.getProductImageURL());
        
        return productRepo.save(existing);
    }

    @Override
    public void deleteProduct(Long id) throws ProductIDNotFoundException
    {
        Optional<Product> existing = productRepo.findByProductId(id);
        if(existing.isEmpty())
        {
            throw new ProductIDNotFoundException(id);
        }
        productRepo.deleteById(id);
    }

    // reserve stock for an oder
    @Transactional
    public void reserveStock(Long productId, int quantity)
    {
        //precautionary check, prodoctId should exist by default
        Product prod = productRepo.findByProductId(productId)
                        .orElseThrow(()-> new ProductIDNotFoundException(productId));
        Inventory inv = prod.getInventory();
        if(inv != null)
        {
            int actualStock = inv.getInStock() - inv.getReservedQty();

            if(actualStock >= quantity)
            {
                inv.setReservedQty(inv.getReservedQty() + quantity);
                // set hold expiry time to 15 mins from now
                inv.setHoldExpiresAt(LocalDateTime.now().plusMinutes(15));
                productRepo.save(prod);
            }
            else
            {
                //throw exception if not enough stock
                throw new InsufficientStockException("Stock for " + prod.getProductName() + " is less than " + quantity);
            }
        }
    }
}