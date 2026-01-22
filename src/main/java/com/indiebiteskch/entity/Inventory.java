package com.indiebiteskch.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "inventory")
@Getter @Setter
public class Inventory {

    //takes productid as primary
    @Id
    @Column(name = "product_id")
    private Long productID;

    @OneToOne
    @MapsId
    @JoinColumn(name = "product_id")
    @JsonBackReference
    private Product product;

    @Column(name = "current_qty")
    @PositiveOrZero(message = "Inventory stock cannot be negative")
    private int inStock;

    // for soft locking items, default at 0; 15 mins
    @Column(name = "reserved_qty")
    private int reservedQty = 0;

    @Column(name = "hold_expires_at")
    private LocalDateTime holdExpiresAt;
    
    public void setProduct(Product product) 
    {
        this.product = product;
        if(product != null)
        {
            //make sure invenID matches productID
            this.productID = product.getProductId();

            //if product's inventory doesn't matches this,
            //synch them
            if(product.getInventory() != this)
            {
                product.setInventory(this);
            }
        }
    }
}
