package com.indiebiteskch.test;

import com.indiebiteskch.entity.Product;
import com.indiebiteskch.exceptions.ProductIDNotFoundException;
import com.indiebiteskch.model.Category;
import com.indiebiteskch.service.ProductService;
import com.indiebiteskch.repository.ProductRepo;
import com.indiebiteskch.dto.CreateProductResponse;

import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.junit.jupiter.api.Test;

public class ProductTest {
    
    @Mock
    private ProductRepo productRepo;

    @InjectMocks
    private ProductService productService;

    Product p1, p2;

    private AutoCloseable closeable;

    @BeforeEach
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        p1  = new Product();
        p1.setProductId(1L);
        p1.setProductName("Test Product");
        p1.setCategory(Category.BEVERAGE);
        p1.setProductPrice(10.15);
        p1.setDescription("This is a test product - beverage.");

        p2  = new Product();
        p2.setProductId(2L);
        p2.setProductName("Test Product2");
        p2.setCategory(Category.CAKE);
        p2.setProductPrice(10.15);
        p2.setDescription("This is a test product - cake.");
    }

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void getProductByIdTest()
    {  
        when(productRepo.findByProductId(1L)).thenReturn(Optional.of(p1));
        Product result = productService.getProductById(1L);
        assertEquals("Test Product", result.getProductName());
        verify(productRepo).findByProductId(1L);
    }

    @Test
    public void getProductsByCategoryTest()
    {
        List<Product> mockList = Arrays.asList(p1, p2);

        when(productRepo.findAllByCategory("beverage")).thenReturn(mockList);

        List<Product> result = productService.getProductsByCategory("beverage");

        assertEquals(2, result.size());
        assertEquals("Test Product", result.get(0).getProductName());
    }

    @Test
    public void getProductsByNameTest()
    {
        List<Product> mockList = Arrays.asList(p1, p2);

        when(productRepo.findByProductNameContainingIgnoreCase("Test")).thenReturn(mockList);

        List<Product> result = productService.getProductsByName("Test");
        assertEquals(2, result.size());
        assertTrue(result.get(0).getProductName().equals("Test Product"));
        assertTrue(result.get(1).getProductName().equals("Test Product2"));
        
    }

    @Test
    public void getAllProductsTest()
    {
        List<Product> mockList = Arrays.asList(p1, p2);

        when(productRepo.findAll()).thenReturn(mockList);

        List<Product> result = productService.getAllProducts();
        assertEquals(mockList, result);
    }
    
    @Test
    public void createOrCheckProductTest_CREATE()
    {
        //return an empty mock object if repo is empty
        when(productRepo.findByProductName(p1.getProductName())).thenReturn(Optional.empty());

        //otherwise return the saved product
        when(productRepo.save(p1)).thenReturn(p1);

        CreateProductResponse response = productService.createOrCheckProduct(p1);

        assertEquals("CREATED", response.getStatus());
        assertEquals(p1.getProductName(), response.getProduct().getProductName());
        //verify repo was called once
        verify(productRepo, times(1)).save(p1);
    }

    @Test
    public void createOrCheckProductTest_EXIST()
    {
        //mock repo and find p1 by productName
        when(productRepo.findByProductName(p1.getProductName())).thenReturn(Optional.of(p1));

        CreateProductResponse response = productService.createOrCheckProduct(p1);

        assertEquals("EXIST", response.getStatus());
        assertEquals(p1.getProductName(), response.getProduct().getProductName());
        //verify that repo was never called
        verify(productRepo, never()).save(any(Product.class));
    }

    @Test
    public void updateProductTest()
    {
        p1.setProductId(1L);
        when(productRepo.findByProductId(p1.getProductId())).thenReturn(Optional.of(p1));

        //return p1 when Mock save the updated version
        when(productRepo.save(any(Product.class))).thenReturn(p1);

        Product temp = new Product();
        //temp product will not have productID
        //only change the name to Update: Test Product
        temp.setProductName("Updated: Test Product");
        temp.setCategory(Category.BEVERAGE);
        temp.setProductPrice(10.15);
        temp.setDescription("This is a test product - beverage.");

        assertFalse(temp.getProductName().equals(p1.getProductName()));

        Product updated = productService.updateProduct(p1.getProductId(), temp);

        assertEquals(temp.getProductName(), updated.getProductName());
        //verify that save was called once from updateProduct method
        verify(productRepo).save(p1);
        
    }

    @Test
    public void deleteProductTest()
    {
        when(productRepo.findByProductId(1L)).thenReturn(Optional.of(p1));

        productService.deleteProduct(1L);

        verify(productRepo, times(1)).findByProductId(1L);
        verify(productRepo, times(1)).deleteById(1L);        
    }

    @Test
    public void deleteProductTest_ExceptionThrown()
    {
        when(productRepo.findByProductId(3L)).thenReturn(Optional.empty());

        assertThrows(ProductIDNotFoundException.class, () -> {
            productService.deleteProduct(3L);
        });
        
        verify(productRepo, never()).deleteById(anyLong());
    }

}