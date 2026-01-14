package com.indiebiteskch.entity;

import com.indiebiteskch.model.Category;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name ="product")
@Getter @Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue
    @Column(name = "product_id")
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_category")
    @NotNull(message = "Pick a category for the product")
    private Category category;

    @Column(name = "product_name", unique = true)
    @NotNull(message = "Product name cannot be null")
    private String productName;

    @Column(name ="product_price")
    @Positive(message = "Product price must be greater than 0")
    private Double productPrice;

    @Column(name = "product_image_url")
    private String productImageURL;

    @Column(name = "descrip", length = 150)
    private String description;
    
    //this is used to autolink tracked inventory products
    //one to one relationship; auto cascade when product is deleted
    //lazy fetch to avoid loading inventory data unless needed (beverage not tracked)
    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Inventory inventory;



    //custom setter for managing bi-directional relationship between Product and Inventory
    public void setInventory(Inventory inventory) {
        if(inventory == null)
        {
            if(this.inventory != null)
            {
                this.inventory.setProduct(null);
            }
        }
        else
        {
            inventory.setProduct(this);
        }
        this.inventory = inventory;
    }

    
}
