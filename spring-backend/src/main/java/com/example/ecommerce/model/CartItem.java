package com.example.ecommerce.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class CartItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Cart cart;

    @ManyToOne
    private Product product;

    private Integer quantity = 1;

    public CartItem() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Integer getQuantity() { return quantity == null ? 0 : quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal subtotal() {
        if (product == null || product.getPrice() == null) return BigDecimal.ZERO;
        return product.getPrice().multiply(BigDecimal.valueOf(getQuantity()));
    }

    @Override
    public String toString() {
        return "CartItem{" + "id=" + id + ", product=" + (product == null ? "null" : product.getName()) + ", quantity=" + quantity + '}';
    }
}
