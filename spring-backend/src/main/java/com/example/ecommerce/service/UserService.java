package com.example.ecommerce.service;

import com.example.ecommerce.model.AppUser;
import com.example.ecommerce.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser u = repo.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        return new org.springframework.security.core.userdetails.User(u.getUsername(), u.getPassword(), u.isEnabled(), true, true, true, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    public AppUser registerUser(String username, String email, String rawPassword) {
        if (repo.findByUsernameIgnoreCase(username).isPresent()) {
            throw new RuntimeException("Usuário já existe");
        }
        AppUser u = new AppUser();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setEnabled(true);
        return repo.save(u);
    }
}
