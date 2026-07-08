package com.example.demo.Dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO for login and registration requests.
 * Carries username and password credentials.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthRequestDto {

    @NotBlank(message = "Username must not be blank")
    private String username;

    @NotBlank(message = "Password must not be blank")
    private String password;
}