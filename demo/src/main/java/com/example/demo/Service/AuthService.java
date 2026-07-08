package com.example.demo.Service;

import com.example.demo.Dto.AuthRequestDto;
import com.example.demo.Dto.AuthResponseDto;
import com.example.demo.Entity.SprintAccount;
import com.example.demo.Repository.SprintAccountRepository;
import com.example.demo.Security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service handling user registration and authentication.
 * Issues JWT tokens upon successful auth.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SprintAccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new user as a LEARNER by default.
     * Throws IllegalArgumentException if the username is already taken.
     *
     * @param dto contains username and plain-text password
     * @return AuthResponseDto with JWT token, username, and role
     */
    public AuthResponseDto register(AuthRequestDto dto) {
        // Check for existing username
        if (accountRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + dto.getUsername());
        }

        // Build and persist new account with BCrypt-encoded password
        SprintAccount account = SprintAccount.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(SprintAccount.Role.LEARNER) // Default role on registration
                .build();

        accountRepository.save(account);

        // Generate JWT for the newly registered user
        User userDetails = new User(
                account.getUsername(),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole().name()))
        );

        String token = jwtService.generateToken(userDetails);

        return AuthResponseDto.builder()
                .token(token)
                .username(account.getUsername())
                .role(account.getRole().name())
                .build();
    }

    /**
     * Authenticates an existing user and returns a JWT.
     * Delegates credential validation to Spring Security's AuthenticationManager.
     *
     * @param dto contains username and plain-text password
     * @return AuthResponseDto with JWT token, username, and role
     */
    public AuthResponseDto authenticate(AuthRequestDto dto) {
        // Spring Security validates credentials; throws BadCredentialsException on failure
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
        );

        // Load the account to get role details for the token
        SprintAccount account = accountRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + dto.getUsername()));

        User userDetails = new User(
                account.getUsername(),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole().name()))
        );

        String token = jwtService.generateToken(userDetails);

        return AuthResponseDto.builder()
                .token(token)
                .username(account.getUsername())
                .role(account.getRole().name())
                .build();
    }
}