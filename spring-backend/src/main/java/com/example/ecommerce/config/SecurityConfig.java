package com.example.ecommerce.config;

import com.example.ecommerce.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http, UserService userService) throws Exception {
        http
          .authorizeHttpRequests(authorize -> authorize
              .requestMatchers(
                  new AntPathRequestMatcher("/"),
                  new AntPathRequestMatcher("/product/**"),
                  new AntPathRequestMatcher("/api/products/**"),
                  new AntPathRequestMatcher("/css/**"),
                  new AntPathRequestMatcher("/js/**"),
                  new AntPathRequestMatcher("/images/**"),
                  new AntPathRequestMatcher("/media/**"),
                  new AntPathRequestMatcher("/static/**"),
                  new AntPathRequestMatcher("/register"),
                  new AntPathRequestMatcher("/login"),
                  new AntPathRequestMatcher("/accounts/**")
              ).permitAll()
              .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
              .anyRequest().authenticated()
          )
          .formLogin(form -> form
              .loginPage("/login")
              .defaultSuccessUrl("/", true)
              .permitAll()
          )
          .logout(logout -> logout
              .logoutUrl("/logout")
              .logoutSuccessUrl("/")
              .permitAll()
          )
          .userDetailsService(userService);
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }

}
