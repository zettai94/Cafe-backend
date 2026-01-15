package com.indiebiteskch.service;

import com.indiebiteskch.dto.CreateProductResponse;
import com.indiebiteskch.entity.Product;
import com.indiebiteskch.repository.ProductRepo;
import com.indiebiteskch.service.interfaces.ProductServiceInterface;
import com.indiebiteskch.exceptions.ProductIDNotFoundException;
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
        return productRepo.findByProductId(id).orElse(null);//orElseThrow(new ProductIDNotFoundException(id));
    }

    @Override
    public List<Product> getProductsByCategory(String category)
    {
        return productRepo.findAllByCategory(category);
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

}