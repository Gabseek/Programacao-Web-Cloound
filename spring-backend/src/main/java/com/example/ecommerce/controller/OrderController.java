package com.example.ecommerce.controller;

import com.example.ecommerce.model.Order;
import com.example.ecommerce.repository.OrderRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:8000")
public class OrderController {
    private final OrderRepository repo;

    public OrderController(OrderRepository repo) { this.repo = repo; }

    @GetMapping
    public List<Order> list(@RequestParam String username) {
        return repo.findByUsernameOrderByCreatedAtDesc(username);
    }

    @GetMapping("/{id}")
    public Order detail(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
    }
}
