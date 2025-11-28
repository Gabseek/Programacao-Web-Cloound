package com.example.ecommerce.controller;

import com.example.ecommerce.service.UserService;
import com.example.ecommerce.model.AppUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

@Controller
public class AuthController {
    private final UserService userService;
    private final AuthenticationManager authManager;

    public AuthController(UserService userService, AuthenticationManager authManager) {
        this.userService = userService;
        this.authManager = authManager;
    }

    @GetMapping({"/login", "/accounts/login/"})
    public String loginView(@RequestParam(value="error", required=false) String error, Model model, Principal principal) {
        if (principal != null) return "redirect:/";
        if (error != null) model.addAttribute("error", "Credenciais inválidas");
        model.addAttribute("pageTitle", "Entrar");
        return "login";
    }

    @GetMapping({"/register", "/accounts/register/"})
    public String registerView(Principal principal, Model model) {
        if (principal != null) return "redirect:/";
        model.addAttribute("pageTitle", "Cadastrar");
        return "register";
    }

    @PostMapping({"/register", "/accounts/register/"})
    public String register(@RequestParam String username, @RequestParam(required = false) String email, @RequestParam String password, Model model, HttpServletRequest request) {
        try {
            AppUser saved = userService.registerUser(username, email, password);

            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
            Authentication auth = authManager.authenticate(token);
            SecurityContextHolder.getContext().setAuthentication(auth);

            request.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
            );

            return "redirect:/";
        } catch (DataIntegrityViolationException ex) {
            model.addAttribute("error", "Este usuário já existe.");
            model.addAttribute("pageTitle", "Cadastrar");
            return "register";
        } catch (RuntimeException ex) {
            model.addAttribute("error", "Este usuário já existe.");
            model.addAttribute("pageTitle", "Cadastrar");
            return "register";
        }
    }
}
