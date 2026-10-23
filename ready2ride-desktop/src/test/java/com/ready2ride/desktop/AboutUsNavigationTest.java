package com.ready2ride.desktop;

import com.ready2ride.core.model.User;
import com.ready2ride.core.model.UserRole;
import com.ready2ride.desktop.util.UserSession;
import com.ready2ride.desktop.view.AboutUsView;
import com.ready2ride.desktop.view.TopNavBar;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class AboutUsNavigationTest {

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // JavaFX runtime already started
        }
    }

    @BeforeEach
    void resetSession() {
        UserSession.getInstance().logout();
    }

    private void runOnFxThread(Runnable action) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        java.util.concurrent.atomic.AtomicReference<Throwable> error = new java.util.concurrent.atomic.AtomicReference<>();
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "JavaFX thread timed out");
        if (error.get() != null) {
            if (error.get() instanceof AssertionError ae) throw ae;
            throw new RuntimeException(error.get());
        }
    }

    @Test
    @DisplayName("Requirement 1: Contact button and phone icon are removed from the shared top navigation bar")
    void testContactRemovedFromHeaderNavigation() throws InterruptedException {
        runOnFxThread(() -> {
            TopNavBar navBar = new TopNavBar(TopNavBar.NavPage.HOME);

            // Find centerNav inside TopNavBar children (HBox containing nav pills)
            HBox centerNav = null;
            for (Node node : navBar.getChildren()) {
                if (node instanceof HBox && node != navBar.getBrandLogoBox()) {
                    centerNav = (HBox) node;
                    break;
                }
            }
            assertNotNull(centerNav, "centerNav container must exist in TopNavBar");

            // Extract text from all buttons in centerNav
            List<String> navTexts = new ArrayList<>();
            for (Node child : centerNav.getChildren()) {
                if (child instanceof Button btn) {
                    navTexts.add(btn.getText().trim());
                }
            }

            // Verify Contact is NOT present
            assertFalse(navTexts.contains("Contact"), "Contact button must be removed from navigation pills");

            // Verify exactly Home, Vehicles, My Bookings, About Us are present
            assertEquals(4, navTexts.size(), "Navigation bar must have exactly 4 main pills");
            assertEquals("Home", navTexts.get(0));
            assertEquals("Vehicles", navTexts.get(1));
            assertEquals("My Bookings", navTexts.get(2));
            assertEquals("About Us", navTexts.get(3));
        });
    }

    @Test
    @DisplayName("Requirement 1: TopNavBar active pill styling updates cleanly for About Us")
    void testAboutUsActivePageStyling() throws InterruptedException {
        runOnFxThread(() -> {
            TopNavBar navBar = new TopNavBar(TopNavBar.NavPage.ABOUT);
            assertNotNull(navBar.getNavAboutBtn());
            assertEquals("About Us", navBar.getNavAboutBtn().getText().trim());

            // Switching pages updates styling
            navBar.setActivePage(TopNavBar.NavPage.HOME);
            assertNotNull(navBar.getNavHomeBtn().getGraphic());

            navBar.setActivePage(TopNavBar.NavPage.ABOUT);
            assertNotNull(navBar.getNavAboutBtn().getGraphic());
        });
    }

    @Test
    @DisplayName("Requirement 2: AboutUsView initializes all required sections and content")
    void testAboutUsViewStructureAndAllSections() throws InterruptedException {
        runOnFxThread(() -> {
            AtomicBoolean homeNavigated = new AtomicBoolean(false);
            AtomicBoolean vehiclesNavigated = new AtomicBoolean(false);
            AtomicBoolean bookingsNavigated = new AtomicBoolean(false);

            AboutUsView aboutUsView = new AboutUsView(
                    () -> homeNavigated.set(true),
                    () -> vehiclesNavigated.set(true),
                    () -> bookingsNavigated.set(true),
                    () -> {},
                    () -> {}
            );

            assertNotNull(aboutUsView.getRootNode(), "AboutUsView rootNode must exist");
            assertNotNull(aboutUsView.getTopNavBar(), "AboutUsView must use shared TopNavBar");
            assertNotNull(aboutUsView.getScrollPane(), "AboutUsView must be scrollable");
            assertTrue(aboutUsView.getScrollPane().isFitToWidth(), "ScrollPane must fit to width for responsiveness");

            // Recursively search for all required texts
            List<String> allLabels = new ArrayList<>();
            collectLabelTexts(aboutUsView.getRootNode(), allLabels);

            // 1. Hero section
            assertTrue(allLabels.stream().anyMatch(t -> t.contains("About Ready2Ride")),
                    "Hero heading 'About Ready2Ride' must be present");
            assertTrue(allLabels.stream().anyMatch(t -> t.contains("Making every journey simple, safe, and memorable.")),
                    "Hero subtitle must match requirement");

            // 2. Our Story
            assertTrue(allLabels.stream().anyMatch(t -> t.contains("Our Story")),
                    "Our Story section must be present");
            assertTrue(allLabels.stream().anyMatch(t -> t.toLowerCase().contains("platform") && t.toLowerCase().contains("rental provider")),
                    "Our Story must explain connection between customers and rental providers");

            // 3. Our Mission
            assertTrue(allLabels.stream().anyMatch(t -> t.contains("Make vehicle rentals simple, convenient, transparent, and accessible")),
                    "Our Mission statement must be present");

            // 4. Why Choose Ready2Ride cards
            assertTrue(allLabels.stream().anyMatch(t -> t.contains("Wide Vehicle Selection")),
                    "Card 'Wide Vehicle Selection' must be present");
            assertTrue(allLabels.stream().anyMatch(t -> t.contains("Affordable Rental Rates")),
                    "Card 'Affordable Rental Rates' must be present");
            assertTrue(allLabels.stream().anyMatch(t -> t.contains("Simple Booking Process")),
                    "Card 'Simple Booking Process' must be present");
            assertTrue(allLabels.stream().anyMatch(t -> t.contains("Trusted and Convenient Experience")),
                    "Card 'Trusted and Convenient Experience' must be present");

            // 5. How It Works steps
            assertTrue(allLabels.stream().anyMatch(t -> t.contains("Browse Vehicles")),
                    "Step 'Browse Vehicles' must be present");
            assertTrue(allLabels.stream().anyMatch(t -> t.contains("Select a Vehicle")),
                    "Step 'Select a Vehicle' must be present");
            assertTrue(allLabels.stream().anyMatch(t -> t.contains("Book & Confirm")),
                    "Step 'Book & Confirm' must be present");
            assertTrue(allLabels.stream().anyMatch(t -> t.contains("Pick Up & Ride")),
                    "Step 'Pick Up & Ride' must be present");

            // 6. For Rental Providers
            assertTrue(allLabels.stream().anyMatch(t -> t.contains("For Rental Providers")),
                    "Section 'For Rental Providers' must be present");
            assertTrue(allLabels.stream().anyMatch(t -> t.contains("List Vehicles")),
                    "Provider feature 'List Vehicles' must be present");
            assertTrue(allLabels.stream().anyMatch(t -> t.contains("Upload Images")),
                    "Provider feature 'Upload Images' must be present");
            assertTrue(allLabels.stream().anyMatch(t -> t.contains("Manage Rental Prices")),
                    "Provider feature 'Manage Rental Prices' must be present");
            assertTrue(allLabels.stream().anyMatch(t -> t.contains("Manage Fleet Inventory")),
                    "Provider feature 'Manage Fleet Inventory' must be present");
        });
    }

    @Test
    @DisplayName("Requirement 2: Explore Vehicles buttons open vehicle catalog")
    void testExploreVehiclesButtonsTriggerCallback() throws InterruptedException {
        runOnFxThread(() -> {
            AtomicBoolean catalogOpened = new AtomicBoolean(false);

            AboutUsView aboutUsView = new AboutUsView(
                    () -> {},
                    () -> catalogOpened.set(true),
                    () -> {},
                    () -> {},
                    () -> {}
            );

            assertNotNull(aboutUsView.getHeroExploreBtn(), "Hero Explore Vehicles button must exist");
            assertNotNull(aboutUsView.getCtaExploreBtn(), "CTA Explore Vehicles button must exist");

            // Fire hero explore button
            aboutUsView.getHeroExploreBtn().fire();
            assertTrue(catalogOpened.get(), "Hero Explore Vehicles button should trigger catalog navigation");

            catalogOpened.set(false);

            // Fire CTA banner explore button
            aboutUsView.getCtaExploreBtn().fire();
            assertTrue(catalogOpened.get(), "CTA Explore Vehicles button should trigger catalog navigation");
        });
    }

    @Test
    @DisplayName("Requirement 3: Functional navigation between About Us, Home, and Vehicles")
    void testNavigationBetweenPages() throws InterruptedException {
        runOnFxThread(() -> {
            AtomicBoolean homeNavigated = new AtomicBoolean(false);
            AtomicBoolean vehiclesNavigated = new AtomicBoolean(false);

            AboutUsView aboutUsView = new AboutUsView(
                    () -> homeNavigated.set(true),
                    () -> vehiclesNavigated.set(true),
                    () -> {},
                    () -> {},
                    () -> {}
            );

            // Clicking Home in header
            aboutUsView.getTopNavBar().getNavHomeBtn().fire();
            assertTrue(homeNavigated.get(), "Clicking Home in header should navigate to Home scene");

            // Clicking Vehicles in header
            aboutUsView.getTopNavBar().getNavVehiclesBtn().fire();
            assertTrue(vehiclesNavigated.get(), "Clicking Vehicles in header should navigate to Vehicles catalog scene");
        });
    }

    @Test
    @DisplayName("Requirement 3: Session preservation and role-based header display on About Us page")
    void testSessionPreservationOnAboutUsPage() throws InterruptedException {
        runOnFxThread(() -> {
            AboutUsView aboutUsView = new AboutUsView(
                    () -> {}, () -> {}, () -> {}, () -> {}, () -> {}
            );

            // 1. Initially logged out: session is not active
            assertFalse(UserSession.getInstance().isLoggedIn());

            // 2. Log in a customer
            User customer = new User("CUST-100", "Dhruv Taneja", "dhruv@example.com", "+91 98765 43210", "hash", UserRole.CUSTOMER, "DL-123");
            UserSession.getInstance().login(customer);
            aboutUsView.getTopNavBar().refreshAuthBox();

            assertTrue(UserSession.getInstance().isLoggedIn());
            assertEquals("Dhruv Taneja", UserSession.getInstance().getDisplayName());

            // 3. Log out cleanly
            UserSession.getInstance().logout();
            aboutUsView.getTopNavBar().refreshAuthBox();
            assertFalse(UserSession.getInstance().isLoggedIn());
        });
    }

    private void collectLabelTexts(Node node, List<String> list) {
        if (node instanceof Label lbl && lbl.getText() != null) {
            list.add(lbl.getText());
        }
        if (node instanceof ScrollPane sp && sp.getContent() != null) {
            collectLabelTexts(sp.getContent(), list);
        }
        if (node instanceof javafx.scene.Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                collectLabelTexts(child, list);
            }
        }
    }
}
