package com.saxion.proj.tfms.order.model;

import java.time.LocalDateTime;

public class Order {

    private Long id;
    private Long customerId;
    private String customerName;
    private String pickupAddress;
    private String deliveryAddress;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum OrderStatus {
        PENDING, ASSIGNED, IN_TRANSIT, DELIVERED, CANCELLED
    }

    // Constructors
    public Order() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = OrderStatus.PENDING;
    }

    public Order(Long customerId, String customerName, String pickupAddress, String deliveryAddress) {
        this();
        this.customerId = customerId;
        this.customerName = customerName;
        this.pickupAddress = pickupAddress;
        this.deliveryAddress = deliveryAddress;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}
