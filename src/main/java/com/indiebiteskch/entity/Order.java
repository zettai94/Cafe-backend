package com.indiebiteskch.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "orders")
@Getter @Setter
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderID;

    @Column(name = "time_ordered")
    private LocalDateTime createdAt;

    @Column(name = "status")
    private String status;

    @Column(name = "total_amount")
    @Positive(message = "Total amount must be positive")
    private BigDecimal total;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List <OrderItem> items;
}
