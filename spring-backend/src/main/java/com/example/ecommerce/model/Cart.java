package com.example.ecommerce.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Cart {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    public Cart() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }

    public void addItem(CartItem it) {
        if (it == null) return;
        items.add(it);
        it.setCart(this);
    }

    public void removeItem(CartItem it) {
        if (it == null) return;
        items.remove(it);
        it.setCart(null);
    }

    @Override
    public String toString() {
        return "Cart{" + "id=" + id + ", username=" + username + '}';
    }
}
