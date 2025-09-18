package com.saxion.proj.tfms.order.controller;

import com.saxion.proj.tfms.order.model.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    // Simple in-memory storage for demo purposes
    private final Map<Long, Order> orders = new HashMap<>();
    private Long nextId = 1L;

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "order-service");
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(new ArrayList<>(orders.values()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Order order = orders.get(id);
        return order != null ? ResponseEntity.ok(order) : ResponseEntity.notFound().build();
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Order>> getOrdersByCustomerId(@PathVariable Long customerId) {
        List<Order> customerOrders = orders.values().stream()
                .filter(order -> order.getCustomerId().equals(customerId))
                .toList();
        return ResponseEntity.ok(customerOrders);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable Order.OrderStatus status) {
        List<Order> ordersWithStatus = orders.values().stream()
                .filter(order -> order.getStatus() == status)
                .toList();
        return ResponseEntity.ok(ordersWithStatus);
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        order.setId(nextId++);
        orders.put(order.getId(), order);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @RequestBody Order orderDetails) {
        Order order = orders.get(id);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        order.setCustomerName(orderDetails.getCustomerName());
        order.setPickupAddress(orderDetails.getPickupAddress());
        order.setDeliveryAddress(orderDetails.getDeliveryAddress());
        order.setStatus(orderDetails.getStatus());
        order.updateTimestamp();

        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long id, @RequestParam Order.OrderStatus status) {
        Order order = orders.get(id);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        order.setStatus(status);
        order.updateTimestamp();
        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        if (orders.remove(id) != null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
