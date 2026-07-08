package com.example.demo.Repository;

import com.example.demo.Entity.SprintAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for SprintAccount entity.
 * Provides authentication lookup by username.
 */
@Repository
public interface SprintAccountRepository extends JpaRepository<SprintAccount, Long> {

    /**
     * Finds a user by their unique username.
     * Used during authentication and registration checks.
     *
     * @param username the login username
     * @return Optional containing the user if found
     */
    Optional<SprintAccount> findByUsername(String username);
}
