package com.example.ecommerce.controller;

import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping
public class ProductController {
    private final ProductRepository repo;

    public ProductController(ProductRepository repo) {
        this.repo = repo;
    }

    @RestController
    @RequestMapping("/api/products")
    static class Api {
        private final ProductRepository repo;
        Api(ProductRepository repo) { this.repo = repo; }
        @GetMapping
        public List<Product> list() { return repo.findAll(); }
        @GetMapping("/{id}")
        public Product detail(@PathVariable Long id) { return repo.findById(id).orElseThrow(() -> new RuntimeException("Produto não encontrado")); }
    }

    @GetMapping("/")
    public String listPage(Model model) {
        List<Product> products = repo.findAll();
        model.addAttribute("products", products);
        model.addAttribute("pageTitle", "Cloound");
        return "product_list";
    }

    @GetMapping("/product/{id}/")
    public String detailPage(@PathVariable Long id, Model model) {
        Product product = repo.findById(id).orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        model.addAttribute("product", product);
        model.addAttribute("pageTitle", product.getName());
        return "product_detail";
    }
}
