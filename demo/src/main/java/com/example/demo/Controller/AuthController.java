package com.example.demo.Controller;

import com.example.demo.Dto.AuthRequestDto;
import com.example.demo.Dto.AuthResponseDto;
import com.example.demo.Service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication endpoints.
 * Base path: /api/auth
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new user account (defaults to LEARNER role).
     *
     * POST /api/auth/register
     *
     * @param dto username and password
     * @return 200 OK with AuthResponseDto (token, username, role)
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody AuthRequestDto dto) {
        return ResponseEntity.ok(authService.register(dto));
    }

    /**
     * Authenticates an existing user and returns a JWT token.
     *
     * POST /api/auth/login
     *
     * @param dto username and password
     * @return 200 OK with AuthResponseDto (token, username, role)
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody AuthRequestDto dto) {
        return ResponseEntity.ok(authService.authenticate(dto));
    }
}