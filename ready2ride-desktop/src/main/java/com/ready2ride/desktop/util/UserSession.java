package com.ready2ride.desktop.util;

import com.ready2ride.core.model.User;
import com.ready2ride.core.model.UserRole;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Thread-safe singleton session context holding the currently authenticated user
 * in the Ready2Ride desktop application.
 */
public final class UserSession {

    private static final UserSession INSTANCE = new UserSession();

    private User currentUser = null;
    private boolean isDemoMode = false;
    private final List<Consumer<User>> listeners = new ArrayList<>();

    private UserSession() {
    }

    public static UserSession getInstance() {
        return INSTANCE;
    }

    public synchronized void login(User user) {
        this.currentUser = user;
        this.isDemoMode = false;
        notifyListeners();
    }

    public synchronized void loginAsDemo() {
        this.currentUser = new User(
                "GUEST-DEMO-001",
                "Guest Explorer",
                "guest@ready2ride.com",
                "+91 00000 00000",
                "",
                UserRole.CUSTOMER,
                "DL-DEMO-0000"
        );
        this.isDemoMode = true;
        notifyListeners();
    }

    public synchronized void logout() {
        this.currentUser = null;
        this.isDemoMode = false;
        notifyListeners();
    }

    public synchronized User getCurrentUser() {
        return currentUser;
    }

    public synchronized boolean isLoggedIn() {
        return currentUser != null;
    }

    public synchronized boolean isDemoMode() {
        return isDemoMode;
    }

    public synchronized boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == UserRole.ADMIN;
    }

    public synchronized boolean isCustomer() {
        return currentUser != null && currentUser.getRole() == UserRole.CUSTOMER;
    }

    public synchronized String getDisplayName() {
        if (currentUser != null) {
            return currentUser.getFullName();
        }
        return isDemoMode ? "Guest User" : "Sign In";
    }

    public synchronized String getUserId() {
        return currentUser != null ? currentUser.getId() : "GUEST-USER";
    }

    public synchronized void addListener(Consumer<User> listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    private synchronized void notifyListeners() {
        for (Consumer<User> l : listeners) {
            try {
                l.accept(currentUser);
            } catch (Exception ignored) {
            }
        }
    }
}
