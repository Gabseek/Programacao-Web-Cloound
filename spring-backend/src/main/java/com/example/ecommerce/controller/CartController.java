package com.example.ecommerce.controller;

import com.example.ecommerce.model.*;
import com.example.ecommerce.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = {"http://localhost:8000", "http://localhost:8081"})
public class CartController {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public CartController(CartRepository cartRepository, ProductRepository productRepository, OrderRepository orderRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @GetMapping
    public Cart getCart(@RequestParam(required = false) String username, Principal principal) {
        String user = username;
        if (principal != null && principal.getName() != null && !principal.getName().isBlank()) user = principal.getName();
        if (user == null || user.isBlank()) throw new RuntimeException("username required");
        final String resolvedUser = user; // make it effectively final for lambda usage

        return cartRepository.findByUsername(resolvedUser).orElseGet(() -> {
            Cart c = new Cart();
            c.setUsername(resolvedUser);
            return cartRepository.save(c);
        });
    }

    @PostMapping("/add/{productId}")
    public ResponseEntity<?> addToCart(
            @PathVariable Long productId,
            @RequestParam(required = false) String username,
            Principal principal
    ) {
        String user = username;
        if (principal != null && principal.getName() != null && !principal.getName().isBlank()) user = principal.getName();
        if (user == null || user.isBlank()) return ResponseEntity.badRequest().body("username required");

        Product p = productRepository.findById(productId).orElse(null);
        if (p == null) return ResponseEntity.status(404).body("Produto não existe");

        Cart cart = getCart(user, principal);
        Optional<CartItem> existing = cart.getItems().stream().filter(it -> it.getProduct().getId().equals(productId)).findFirst();
        if (existing.isPresent()) {
            CartItem it = existing.get();
            it.setQuantity(it.getQuantity() + 1);
        } else {
            CartItem it = new CartItem();
            it.setProduct(p);
            it.setQuantity(1);
            cart.addItem(it);
        }
        cartRepository.save(cart);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/remove/{itemId}")
    public ResponseEntity<?> removeFromCart(
            @PathVariable Long itemId,
            @RequestParam(required = false) String username,
            Principal principal
    ) {
        String user = username;
        if (principal != null && principal.getName() != null && !principal.getName().isBlank()) user = principal.getName();
        if (user == null || user.isBlank()) return ResponseEntity.badRequest().body("username required");

        Cart cart = getCart(user, principal);
        boolean removed = cart.getItems().removeIf(it -> it.getId() != null && it.getId().equals(itemId));
        if (removed) {
            cartRepository.save(cart);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(404).body("Item não encontrado no carrinho");
        }
    }

    @PostMapping("/update/{itemId}")
    public ResponseEntity<?> updateCart(
            @PathVariable Long itemId,
            @RequestParam(required = false) String username,
            @RequestParam Integer quantity,
            Principal principal
    ) {
        String user = username;
        if (principal != null && principal.getName() != null && !principal.getName().isBlank()) user = principal.getName();
        if (user == null || user.isBlank()) return ResponseEntity.badRequest().body("username required");

        Cart cart = getCart(user, principal);
        Optional<CartItem> opt = cart.getItems().stream().filter(it -> it.getId() != null && it.getId().equals(itemId)).findFirst();
        if (opt.isPresent()) {
            CartItem it = opt.get();
            it.setQuantity(Math.max(1, quantity));
            cartRepository.save(cart);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(404).body("Item não encontrado no carrinho");
        }
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestParam(required = false) String username, Principal principal) {
        String user = username;
        if (principal != null && principal.getName() != null && !principal.getName().isBlank()) user = principal.getName();
        if (user == null || user.isBlank()) return ResponseEntity.badRequest().body("username required");

        Cart cart = getCart(user, principal);
        if (cart.getItems().isEmpty()) return ResponseEntity.badRequest().body("Carrinho vazio");

        Order order = new Order();
        order.setUsername(user);
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem it : cart.getItems()) {
            OrderItem oi = new OrderItem();
            oi.setProductId(it.getProduct().getId());
            oi.setName(it.getProduct().getName());
            oi.setPrice(it.getProduct().getPrice());
            oi.setQuantity(it.getQuantity());
            oi.setSubtotal(it.subtotal());
            order.addItem(oi);
            total = total.add(oi.getSubtotal());
        }
        order.setTotal(total);
        orderRepository.save(order);

        cart.getItems().clear();
        cartRepository.save(cart);

        return ResponseEntity.ok(order);
    }
}
