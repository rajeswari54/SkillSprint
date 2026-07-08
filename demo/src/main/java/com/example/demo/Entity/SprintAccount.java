package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing an authenticated user in the SkillSprint system.
 * Maps to the 'system_user' table.
 */
@Entity
@Table(name = "system_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SprintAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Must be unique across all users. Used as the login identifier. */
    @Column(nullable = false, unique = true)
    private String username;

    /** Stores BCrypt-encoded password only. Never plain text. */
    @Column(nullable = false)
    private String password;

    /** Role determines access level: LEARNER, LINGUIST, or ADMIN. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * Roles available in the SkillSprint system.
     */
    public enum Role {
        LEARNER,    // Student: can track own progress
        LINGUIST,   // Mentor: can create/manage roadmaps and milestones
        ADMIN       // Administrator: full access including deletions
    }
}