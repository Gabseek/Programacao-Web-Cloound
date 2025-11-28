package com.example.ecommerce.controller;

import com.example.ecommerce.model.*;
import com.example.ecommerce.repository.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.*;

@Controller
@RequestMapping("/cart")
public class CartPageController {
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;

    public CartPageController(ProductRepository productRepository, CartRepository cartRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
    }

    private Map<Long, Integer> sessionCart(HttpSession session) {
        Map<Long, Integer> map = (Map<Long, Integer>) session.getAttribute("cart");
        if (map == null) {
            map = new HashMap<>();
            session.setAttribute("cart", map);
        }
        return map;
    }

    @GetMapping("/")
    public String cartDetail(Model model, Principal principal, HttpSession session) {
        List<Map<String, Object>> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        if (principal != null) {
            String username = principal.getName();
            Cart cart = cartRepository.findByUsername(username).orElseGet(() -> {
                Cart c = new Cart(); c.setUsername(username); return cartRepository.save(c);
            });
            for (CartItem it : cart.getItems()) {
                Map<String, Object> li = new HashMap<>();
                li.put("id", it.getId());
                li.put("product", it.getProduct());
                li.put("quantity", it.getQuantity());
                BigDecimal sub = it.subtotal();
                li.put("subtotal", sub);
                total = total.add(sub);
                items.add(li);
            }
        } else {
            Map<Long, Integer> sess = sessionCart(session);
            if (!sess.isEmpty()) {
                List<Long> pids = new ArrayList<>(sess.keySet());
                List<Product> products = productRepository.findAllById(pids);
                for (Product p : products) {
                    int qty = sess.getOrDefault(p.getId(), 0);
                    BigDecimal sub = p.getPrice().multiply(BigDecimal.valueOf(qty));
                    Map<String, Object> li = new HashMap<>();
                    li.put("id", p.getId());
                    li.put("product", p);
                    li.put("quantity", qty);
                    li.put("subtotal", sub);
                    total = total.add(sub);
                    items.add(li);
                }
            }
        }

        model.addAttribute("items", items);
        model.addAttribute("total", total);
        model.addAttribute("pageTitle", "Carrinho de Compras");
        return "cart_detail";
    }

    @GetMapping("/add/{productId}/")
    public String addToCart(@PathVariable Long productId, Principal principal, HttpSession session, RedirectAttributes redirectAttrs) {
        try {
            Product p = productRepository.findById(productId).orElse(null);
            if (p == null) {
                redirectAttrs.addAttribute("msg", "Produto não encontrado");
                redirectAttrs.addAttribute("msgType", "danger");
                return "redirect:/";
            }
            if (principal != null) {
                String username = principal.getName();
                Cart cart = cartRepository.findByUsername(username).orElseGet(() -> { Cart c = new Cart(); c.setUsername(username); return cartRepository.save(c); });
                Optional<CartItem> op = cart.getItems().stream().filter(it -> it.getProduct().getId().equals(productId)).findFirst();
                if (op.isPresent()) {
                    CartItem it = op.get(); it.setQuantity(it.getQuantity() + 1);
                } else {
                    CartItem it = new CartItem(); it.setProduct(p); it.setQuantity(1); cart.addItem(it);
                }
                cartRepository.save(cart);
            } else {
                Map<Long, Integer> sess = sessionCart(session);
                sess.put(productId, sess.getOrDefault(productId, 0) + 1);
                session.setAttribute("cart", sess);
            }
            redirectAttrs.addAttribute("msg", "Produto adicionado ao carrinho");
            redirectAttrs.addAttribute("msgType", "success");
            return "redirect:/cart/";
        } catch (Exception ex) {
            ex.printStackTrace();
            redirectAttrs.addAttribute("msg", "Erro ao adicionar ao carrinho");
            redirectAttrs.addAttribute("msgType", "danger");
            return "redirect:/";
        }
    }

    @GetMapping("/remove/{id}/")
    public String removeFromCart(@PathVariable Long id, Principal principal, HttpSession session) {
        if (principal != null) {
            String username = principal.getName();
            Cart cart = cartRepository.findByUsername(username).orElse(null);
            if (cart != null) {
                cart.getItems().removeIf(it -> Objects.equals(it.getId(), id));
                cartRepository.save(cart);
            }
        } else {
            Map<Long, Integer> sess = sessionCart(session);
            sess.remove(id);
            session.setAttribute("cart", sess);
        }
        return "redirect:/cart/";
    }

    @PostMapping("/update/{id}/")
    public String updateCart(@PathVariable Long id, @RequestParam Integer quantity, Principal principal, HttpSession session) {
        if (principal != null) {
            String username = principal.getName();
            Cart cart = cartRepository.findByUsername(username).orElse(null);
            if (cart != null) {
                cart.getItems().stream().filter(it -> Objects.equals(it.getId(), id)).findFirst().ifPresent(it -> {
                    it.setQuantity(Math.max(1, quantity));
                });
                cartRepository.save(cart);
            }
        } else {
            Map<Long, Integer> sess = sessionCart(session);
            if (quantity <= 0) sess.remove(id);
            else sess.put(id, Math.max(1, quantity));
            session.setAttribute("cart", sess);
        }
        return "redirect:/cart/";
    }

    @PostMapping("/checkout/")
    public String checkout(Principal principal, HttpSession session) {
        if (principal == null) {
            return "redirect:/login?next=/cart/checkout/";
        }
        String username = principal.getName();
        Cart cart = cartRepository.findByUsername(username).orElse(null);
        if (cart == null || cart.getItems().isEmpty()) {
            return "redirect:/cart/";
        }
        Order order = new Order();
        order.setUsername(username);
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
        return "redirect:/cart/checkout/success/?order_id=" + order.getId();
    }

    @GetMapping("/checkout/success/")
    public String checkoutSuccess(@RequestParam("order_id") Long orderId, Model model) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
        model.addAttribute("order", order);
        model.addAttribute("pageTitle", "Pedido #" + order.getId());
        return "checkout_success";
    }
}
