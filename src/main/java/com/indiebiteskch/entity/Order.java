package com.indiebiteskch.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
@Table(name = "cafe_order")
@Getter @Setter
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderID;

    @Column(name = "order_date", updatable = false)
    private LocalDateTime createdAt;

    // hibernate will handle setting createdAt before persisting
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Column(name = "status")
    @NotNull(message = "Order status cannot be null")
    private String status = "PENDING"; //default value; considering ENUM later

    @Column(name = "total_amount")
    @PositiveOrZero(message = "Total amount cannot be negative")
    private BigDecimal total;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List <OrderItem> orderList = new ArrayList<>();

    //helper method to set both sides of relationship
    public void addItem(OrderItem item)
    {
        orderList.add(item);
        item.setOrder(this);
    }
}
