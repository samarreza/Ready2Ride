package com.ready2ride.core.repository;

import com.ready2ride.core.model.User;
import com.ready2ride.core.model.UserRole;

import java.util.List;
import java.util.Optional;

/**
 * Data access abstraction for {@link User} accounts.
 * Provides lookup and persistence operations across both customers and fleet administrators.
 */
public interface UserRepository {

    /**
     * Persists or updates a user entity.
     *
     * @param user user entity to save
     * @return persisted user entity
     */
    User save(User user);

    /**
     * Finds a user by unique identifier.
     *
     * @param id user ID
     * @return {@link Optional} containing user if found, empty otherwise
     */
    Optional<User> findById(String id);

    /**
     * Finds a user by email address (case-insensitive).
     *
     * @param email user email address
     * @return {@link Optional} containing user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Retrieves all registered users.
     *
     * @return unmodifiable list of users
     */
    List<User> findAll();

    /**
     * Retrieves all users having a specific role.
     *
     * @param role {@link UserRole}
     * @return list of matching users
     */
    List<User> findByRole(UserRole role);

    /**
     * Checks if a user with the specified email address exists.
     *
     * @param email user email address
     * @return true if exists; false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Checks if a user with the specified identifier exists.
     *
     * @param id user ID
     * @return true if exists; false otherwise
     */
    boolean existsById(String id);

    /**
     * Deletes (or deactivates) a user by identifier.
     *
     * @param id user ID
     * @return true if removed; false otherwise
     */
    boolean deleteById(String id);
}
