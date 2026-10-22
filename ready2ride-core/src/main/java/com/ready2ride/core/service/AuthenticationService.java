package com.ready2ride.core.service;

import com.ready2ride.core.exception.AuthenticationException;
import com.ready2ride.core.exception.UserAlreadyExistsException;
import com.ready2ride.core.model.User;
import com.ready2ride.core.model.UserRole;
import com.ready2ride.core.repository.UserRepository;
import com.ready2ride.core.util.PasswordHasher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Service orchestrating user authentication, customer registration, and controlled administrator provisioning.
 * Employs PBKDF2 with HMAC-SHA256 password hashing and prevents credential leakage in logs or exceptions.
 */
public class AuthenticationService {

    private final UserRepository userRepository;

    public AuthenticationService(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository, "UserRepository must not be null");
    }

    /**
     * Authenticates a user using email/identifier and raw password.
     *
     * @param identifier  email address or user ID
     * @param rawPassword plaintext candidate password
     * @return verified {@link User} entity
     * @throws AuthenticationException if credentials are invalid or account is deactivated
     */
    public User authenticate(String identifier, String rawPassword) {
        if (identifier == null || identifier.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            throw new AuthenticationException("Please enter both your email/username and password.");
        }

        String normalized = identifier.trim().toLowerCase();

        // 1. Look up user by normalized email first, then by ID/username
        Optional<User> userOpt = userRepository.findByEmail(normalized);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findById(identifier.trim());
        }

        if (userOpt.isEmpty()) {
            // Generic message to prevent account enumeration attacks
            throw new AuthenticationException("Invalid credentials. Please verify your email and password.");
        }

        User user = userOpt.get();

        // 2. Verify candidate password against stored secure hash
        if (!PasswordHasher.checkPassword(rawPassword, user.getPasswordHash())) {
            throw new AuthenticationException("Invalid credentials. Please verify your email and password.");
        }

        // 3. Reject inactive accounts
        if (!user.isActive()) {
            throw new AuthenticationException("Your account is deactivated. Please contact customer support.");
        }

        // 4. Return authenticated user
        return user;
    }

    /**
     * Registers a new customer account.
     * Public self-registration is strictly restricted to the {@link UserRole#CUSTOMER} role.
     *
     * @param fullName             customer's full name
     * @param email                unique email address
     * @param phoneNumber          contact phone number
     * @param rawPassword          plaintext password
     * @param drivingLicenseNumber driver's license number
     * @return persisted customer {@link User}
     * @throws UserAlreadyExistsException if email is already taken
     * @throws IllegalArgumentException   if required parameters are missing or invalid
     */
    public User registerCustomer(String fullName, String email, String phoneNumber,
                                 String rawPassword, String drivingLicenseNumber) {
        validateRegistrationInputs(fullName, email, phoneNumber, rawPassword);

        String normalizedEmail = email.trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new UserAlreadyExistsException("An account with email '" + normalizedEmail + "' already exists.");
        }

        String id = "CUST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String passwordHash = PasswordHasher.hashPassword(rawPassword);

        User customer = new User(
                id,
                fullName.trim(),
                normalizedEmail,
                phoneNumber.trim(),
                passwordHash,
                UserRole.CUSTOMER,
                drivingLicenseNumber != null ? drivingLicenseNumber.trim() : null,
                true,
                LocalDateTime.now()
        );

        return userRepository.save(customer);
    }

    /**
     * Safe bootstrap operation to create the initial administrator account.
     * Strictly refuses to overwrite if any administrator already exists in the system.
     *
     * @param fullName    administrator's full name
     * @param email       unique administrator email
     * @param phoneNumber contact phone number
     * @param rawPassword secure initial password
     * @return created administrator {@link User}
     * @throws IllegalStateException      if an administrator already exists
     * @throws UserAlreadyExistsException if the email is already in use
     */
    public User bootstrapInitialAdmin(String fullName, String email, String phoneNumber, String rawPassword) {
        validateRegistrationInputs(fullName, email, phoneNumber, rawPassword);

        // Ensure no administrator exists yet
        List<User> existingAdmins = userRepository.findByRole(UserRole.ADMIN);
        if (!existingAdmins.isEmpty()) {
            throw new IllegalStateException("An administrator already exists. Refusing to overwrite existing administrator.");
        }

        String normalizedEmail = email.trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new UserAlreadyExistsException("An account with email '" + normalizedEmail + "' already exists.");
        }

        String id = "ADMIN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String passwordHash = PasswordHasher.hashPassword(rawPassword);

        User admin = new User(
                id,
                fullName.trim(),
                normalizedEmail,
                phoneNumber.trim(),
                passwordHash,
                UserRole.ADMIN,
                null,
                true,
                LocalDateTime.now()
        );

        return userRepository.save(admin);
    }

    /**
     * Updates profile details of an authenticated customer in PostgreSQL.
     */
    public User updateProfile(String userId, String fullName, String phoneNumber, String drivingLicenseNumber) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID '" + userId + "' was not found."));
        if (fullName != null && !fullName.isBlank()) {
            user.setFullName(fullName.trim());
        }
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            user.setPhoneNumber(phoneNumber.trim());
        }
        if (drivingLicenseNumber != null) {
            user.setDrivingLicenseNumber(drivingLicenseNumber.trim());
        }
        return userRepository.save(user);
    }

    /**
     * Retrieves all user accounts in PostgreSQL (for admin management).
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Toggles the active status of an account.
     */
    public User toggleUserActive(String userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID '" + userId + "' was not found."));
        user.setActive(active);
        return userRepository.save(user);
    }

    /**
     * Checks if an administrator account has already been provisioned.
     *
     * @return true if at least one administrator exists
     */
    public boolean hasAdministrator() {
        return !userRepository.findByRole(UserRole.ADMIN).isEmpty();
    }

    private void validateRegistrationInputs(String fullName, String email, String phoneNumber, String rawPassword) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name must not be blank.");
        }
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("A valid email address is required.");
        }
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("Phone number must not be blank.");
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters in length.");
        }
    }
}
