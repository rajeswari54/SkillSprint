package com.example.demo.Dto;

import lombok.*;

/**
 * DTO returned after successful registration or login.
 * Contains the JWT token, username, and assigned role.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDto {

    /** JWT Bearer token for authenticated requests. */
    private String token;

    /** The authenticated user's username. */
    private String username;

    /** The role assigned to this user (LEARNER, LINGUIST, ADMIN). */
    private String role;
}