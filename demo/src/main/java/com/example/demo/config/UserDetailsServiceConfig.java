package com.example.demo.config;

import com.example.demo.Repository.SprintAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

/**
 * Separate configuration class that exclusively owns the UserDetailsService bean.
 *
 * WHY THIS EXISTS — Circular dependency breakdown:
 *
 *   BEFORE (circular):
 *     JwtAuthenticationFilter
 *         └── needs UserDetailsService
 *                 └── defined inside SecurityConfig
 *                         └── needs JwtAuthenticationFilter   ← CYCLE
 *
 *   AFTER (clean):
 *     UserDetailsServiceConfig   (no dependency on Filter or SecurityConfig)
 *         └── produces UserDetailsService bean
 *
 *     JwtAuthenticationFilter
 *         └── needs UserDetailsService  ← from UserDetailsServiceConfig ✓
 *
 *     SecurityConfig
 *         └── needs JwtAuthenticationFilter ✓
 *         └── needs UserDetailsService      ← from UserDetailsServiceConfig ✓
 *
 *   No cycle exists anymore.
 */
@Configuration
@RequiredArgsConstructor
public class UserDetailsServiceConfig {

    private final SprintAccountRepository sprintAccountRepository;

    /**
     * Loads a SprintAccount from the database by username and wraps it
     * as a Spring Security UserDetails object.
     * Role is prefixed with "ROLE_" as required by Spring Security.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> sprintAccountRepository.findByUsername(username)
                .map(account -> new User(
                        account.getUsername(),
                        account.getPassword(),
                        List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole().name()))
                ))
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found: " + username));
    }
}