package com.ready2ride.core.service;

import com.ready2ride.core.exception.AuthenticationException;
import com.ready2ride.core.exception.UserAlreadyExistsException;
import com.ready2ride.core.model.User;
import com.ready2ride.core.model.UserRole;
import com.ready2ride.core.repository.InMemoryUserRepository;
import com.ready2ride.core.util.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthenticationServiceTest {

    private InMemoryUserRepository userRepository;
    private AuthenticationService authService;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        authService = new AuthenticationService(userRepository);
    }

    @Test
    @DisplayName("1. Authenticating valid customer credentials returns verified account")
    void testAuthenticateCustomerSuccess() {
        User customer = authService.registerCustomer(
                "Jane Doe", "jane.doe@example.com", "+1-555-0101", "Password123!", "DL-12345"
        );

        User authenticated = authService.authenticate("jane.doe@example.com", "Password123!");
        assertNotNull(authenticated);
        assertEquals(customer.getId(), authenticated.getId());
        assertEquals(UserRole.CUSTOMER, authenticated.getRole());
        assertTrue(authenticated.isCustomer());
        assertFalse(authenticated.isAdmin());
    }

    @Test
    @DisplayName("2. Normalizing email address case during authentication")
    void testEmailNormalization() {
        authService.registerCustomer(
                "Jane Doe", "jane.doe@example.com", "+1-555-0101", "Password123!", "DL-12345"
        );

        // Enter email in uppercase with leading/trailing spaces
        User authenticated = authService.authenticate("  JANE.DOE@EXAMPLE.COM  ", "Password123!");
        assertNotNull(authenticated);
        assertEquals("jane.doe@example.com", authenticated.getEmail());
    }

    @Test
    @DisplayName("3. Rejecting invalid password with AuthenticationException")
    void testAuthenticateWrongPassword() {
        authService.registerCustomer(
                "Jane Doe", "jane.doe@example.com", "+1-555-0101", "Password123!", "DL-12345"
        );

        assertThrows(AuthenticationException.class,
                () -> authService.authenticate("jane.doe@example.com", "WrongPassword!"));
    }

    @Test
    @DisplayName("4. Rejecting non-existent email with AuthenticationException")
    void testAuthenticateUnknownEmail() {
        assertThrows(AuthenticationException.class,
                () -> authService.authenticate("nonexistent@example.com", "AnyPassword123"));
    }

    @Test
    @DisplayName("5. Rejecting deactivated accounts with AuthenticationException")
    void testAuthenticateDeactivatedAccount() {
        User customer = authService.registerCustomer(
                "Inactive User", "inactive@example.com", "+1-555-0102", "Password123!", null
        );
        customer.setActive(false);
        userRepository.save(customer);

        AuthenticationException ex = assertThrows(AuthenticationException.class,
                () -> authService.authenticate("inactive@example.com", "Password123!"));
        assertTrue(ex.getMessage().toLowerCase().contains("deactivated") || ex.getMessage().toLowerCase().contains("inactive"));
    }

    @Test
    @DisplayName("6. Customer self-registration strictly enforces CUSTOMER role")
    void testRegisterCustomerRoleEnforcement() {
        User customer = authService.registerCustomer(
                "Alice Smith", "alice@example.com", "+1-555-0103", "SecurePass123", "DL-9999"
        );

        assertEquals(UserRole.CUSTOMER, customer.getRole());
        assertTrue(PasswordHasher.checkPassword("SecurePass123", customer.getPasswordHash()));
        assertTrue(customer.isActive());
    }

    @Test
    @DisplayName("7. Rejecting duplicate email during customer registration")
    void testRegisterDuplicateEmailRejection() {
        authService.registerCustomer(
                "User One", "shared@example.com", "+1-555-0104", "Password123", null
        );

        assertThrows(UserAlreadyExistsException.class, () -> authService.registerCustomer(
                "User Two", "SHARED@example.com", "+1-555-0105", "DifferentPass123", null
        ));
    }

    @Test
    @DisplayName("8. Rejecting short passwords during registration")
    void testRegisterShortPasswordRejection() {
        assertThrows(IllegalArgumentException.class, () -> authService.registerCustomer(
                "Short Pass", "short@example.com", "+1-555-0106", "123", null
        ));
    }

    @Test
    @DisplayName("9. Bootstrap initial administrator succeeds when no admin exists")
    void testBootstrapInitialAdmin() {
        assertFalse(authService.hasAdministrator());

        User admin = authService.bootstrapInitialAdmin(
                "System Administrator", "admin@ready2ride.internal", "+1-555-0100", "AdminMaster2026!"
        );

        assertNotNull(admin);
        assertEquals(UserRole.ADMIN, admin.getRole());
        assertTrue(admin.isAdmin());
        assertTrue(authService.hasAdministrator());

        // Successfully authenticate admin
        User verified = authService.authenticate("admin@ready2ride.internal", "AdminMaster2026!");
        assertEquals(admin.getId(), verified.getId());
    }

    @Test
    @DisplayName("10. Bootstrap refuses to overwrite existing administrator")
    void testBootstrapRefusesOverwrite() {
        authService.bootstrapInitialAdmin(
                "Primary Admin", "admin1@ready2ride.internal", "+1-555-0100", "AdminPass123"
        );

        // Attempting to run bootstrap a second time must fail
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                authService.bootstrapInitialAdmin(
                        "Secondary Admin", "admin2@ready2ride.internal", "+1-555-0200", "AdminPass456"
                ));
        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    @DisplayName("11. Registering rental provider succeeds and sets RENTAL_PROVIDER role")
    void testRegisterRentalProvider() {
        User provider = authService.registerRentalProvider(
                "Apex Rentals", "host@apexrentals.com", "+91-9876543210", "HostPass123!", "HOST-LIC-9988"
        );

        assertNotNull(provider);
        assertEquals(UserRole.RENTAL_PROVIDER, provider.getRole());
        assertTrue(provider.isRentalProvider());
        assertFalse(provider.isCustomer());
        assertFalse(provider.isAdmin());

        User authenticated = authService.authenticate("host@apexrentals.com", "HostPass123!");
        assertNotNull(authenticated);
        assertEquals(provider.getId(), authenticated.getId());
        assertEquals(UserRole.RENTAL_PROVIDER, authenticated.getRole());
    }

    @Test
    @DisplayName("12. Public registration gateway rejects ADMIN role self-assignment")
    void testRegisterUserRejectsAdminRole() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                authService.registerUser(
                        "Hacker Admin", "hack@example.com", "+1-555-9999", "Pass123!", null, UserRole.ADMIN
                ));
        assertTrue(ex.getMessage().contains("Public registration permits only CUSTOMER or RENTAL_PROVIDER"));
    }
}
