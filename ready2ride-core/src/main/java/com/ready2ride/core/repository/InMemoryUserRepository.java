package com.ready2ride.core.repository;

import com.ready2ride.core.model.User;
import com.ready2ride.core.model.UserRole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory implementation of {@link UserRepository} for unit testing.
 */
public class InMemoryUserRepository implements UserRepository {

    private final Map<String, User> storage = new ConcurrentHashMap<>();

    @Override
    public User save(User user) {
        Objects.requireNonNull(user, "User must not be null");
        storage.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        String normalized = email.trim().toLowerCase();
        return storage.values().stream()
                .filter(u -> normalized.equalsIgnoreCase(u.getEmail()))
                .findFirst();
    }

    @Override
    public List<User> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(storage.values()));
    }

    @Override
    public List<User> findByRole(UserRole role) {
        if (role == null) {
            return Collections.emptyList();
        }
        return storage.values().stream()
                .filter(u -> u.getRole() == role)
                .toList();
    }

    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    @Override
    public boolean existsById(String id) {
        return id != null && storage.containsKey(id);
    }

    @Override
    public boolean deleteById(String id) {
        return id != null && storage.remove(id) != null;
    }

    public void clear() {
        storage.clear();
    }
}
