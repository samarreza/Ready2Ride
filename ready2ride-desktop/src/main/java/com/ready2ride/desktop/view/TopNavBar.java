package com.ready2ride.desktop.view;

import com.ready2ride.core.model.User;
import com.ready2ride.desktop.util.UiTheme;
import com.ready2ride.desktop.util.UserSession;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Shared, reusable top navigation header component used across Ready2Ride pages
 * (Home, Vehicles Catalog, etc.) ensuring 100% visual, layout, font, and behavioral consistency.
 */
public class TopNavBar extends HBox {

    public enum NavPage {
        HOME,
        VEHICLES,
        MY_BOOKINGS,
        HOW_IT_WORKS,
        ABOUT,
        CONTACT,
        NONE
    }

    private final HBox brandLogoBox;
    private final Button navHomeBtn;
    private final Button navVehiclesBtn;
    private final Button navMyBookingsBtn;
    private final Button navAboutBtn;
    private final Button navContactBtn;
    private final Button navSearchBtn;
    private final Button navLoginBtn;
    private final Button navRegisterBtn;
    private final HBox rightActions;

    private NavPage currentPage;

    private Runnable onNavigateHome;
    private Runnable onNavigateVehicles;
    private Runnable onNavigateMyBookings;
    private Runnable onNavigateAboutUs;
    private Runnable onNavigateContact;
    private Runnable onSearch;
    private Runnable onLogin;
    private Runnable onRegister;
    private Runnable onLogout;

    private static Runnable defaultOnNavigateAboutUs;

    public static void setDefaultOnNavigateAboutUs(Runnable runnable) {
        defaultOnNavigateAboutUs = runnable;
    }

    public static Runnable getDefaultOnNavigateAboutUs() {
        return defaultOnNavigateAboutUs;
    }

    public TopNavBar(NavPage initialPage) {
        this.currentPage = initialPage != null ? initialPage : NavPage.NONE;

        setPrefHeight(72);
        setMinHeight(72);
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(10, 36, 10, 36));
        setBackground(UiTheme.fillBackground(UiTheme.COLOR_WHITE));
        setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 0, 1));
        setEffect(UiTheme.subtleShadow());

        // 1. Left brand logo
        brandLogoBox = UiTheme.createBrandHeaderLogo();
        brandLogoBox.setCursor(Cursor.HAND);
        brandLogoBox.setOnMouseClicked(e -> {
            if (onNavigateHome != null) {
                onNavigateHome.run();
            }
        });

        // 2. Center navigation pills container (Contact removed per requirements)
        HBox centerNav = new HBox(6);
        centerNav.setAlignment(Pos.CENTER);
        centerNav.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_BACKGROUND_LIGHT, 24));
        centerNav.setPadding(new Insets(4, 6, 4, 6));

        navHomeBtn = createNavPill("Home", UiTheme.ICON_HOME, currentPage == NavPage.HOME);
        navVehiclesBtn = createNavPill("Vehicles", UiTheme.ICON_CAR, currentPage == NavPage.VEHICLES);
        navMyBookingsBtn = createNavPill("My Bookings", UiTheme.ICON_CALENDAR, currentPage == NavPage.MY_BOOKINGS);
        navAboutBtn = createNavPill("About Us", UiTheme.ICON_INFO, currentPage == NavPage.ABOUT);
        navContactBtn = createNavPill("Contact", UiTheme.ICON_PHONE, currentPage == NavPage.CONTACT);

        centerNav.getChildren().addAll(navHomeBtn, navVehiclesBtn, navMyBookingsBtn, navAboutBtn);

        // 3. Right side action buttons
        navSearchBtn = new Button();
        navSearchBtn.setGraphic(UiTheme.createSvgIcon(UiTheme.ICON_SEARCH, 18, UiTheme.COLOR_TEXT_DARK));
        navSearchBtn.setBackground(UiTheme.roundedBackground(Color.TRANSPARENT, 20));
        navSearchBtn.setPadding(new Insets(8));
        navSearchBtn.setCursor(Cursor.HAND);
        navSearchBtn.setOnMouseEntered(e -> navSearchBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY_LIGHT, 20)));
        navSearchBtn.setOnMouseExited(e -> navSearchBtn.setBackground(UiTheme.roundedBackground(Color.TRANSPARENT, 20)));
        navSearchBtn.setOnAction(e -> {
            if (onSearch != null) onSearch.run();
        });

        navLoginBtn = new Button("Login");
        navLoginBtn.setGraphic(UiTheme.createSvgIcon(UiTheme.ICON_USER, 16, UiTheme.COLOR_PRIMARY));
        navLoginBtn.setFont(UiTheme.FONT_BODY_BOLD);
        navLoginBtn.setTextFill(UiTheme.COLOR_PRIMARY);
        navLoginBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 8));
        navLoginBtn.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_PRIMARY_BORDER, 8, 1.5));
        navLoginBtn.setPadding(new Insets(8, 20, 8, 16));
        navLoginBtn.setCursor(Cursor.HAND);
        navLoginBtn.setOnMouseEntered(e -> {
            navLoginBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY_LIGHT, 8));
            navLoginBtn.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_PRIMARY, 8, 1.5));
        });
        navLoginBtn.setOnMouseExited(e -> {
            navLoginBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 8));
            navLoginBtn.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_PRIMARY_BORDER, 8, 1.5));
        });
        navLoginBtn.setOnAction(e -> {
            if (onLogin != null) onLogin.run();
        });

        navRegisterBtn = new Button("Register");
        navRegisterBtn.setGraphic(UiTheme.createSvgIcon(UiTheme.ICON_USER_PLUS, 16, UiTheme.COLOR_WHITE));
        navRegisterBtn.setFont(UiTheme.FONT_BODY_BOLD);
        navRegisterBtn.setTextFill(UiTheme.COLOR_WHITE);
        navRegisterBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY, 8));
        navRegisterBtn.setBorder(Border.EMPTY);
        navRegisterBtn.setPadding(new Insets(9, 22, 9, 18));
        navRegisterBtn.setCursor(Cursor.HAND);
        navRegisterBtn.setOnMouseEntered(e -> navRegisterBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY_HOVER, 8)));
        navRegisterBtn.setOnMouseExited(e -> navRegisterBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY, 8)));
        navRegisterBtn.setOnAction(e -> {
            if (onRegister != null) onRegister.run();
        });

        rightActions = new HBox(12);
        rightActions.setAlignment(Pos.CENTER_RIGHT);

        Region spacer1 = new Region();
        Region spacer2 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        getChildren().addAll(brandLogoBox, spacer1, centerNav, spacer2, rightActions);

        // Setup default navigation handlers
        navHomeBtn.setOnAction(e -> {
            if (onNavigateHome != null) onNavigateHome.run();
        });
        navVehiclesBtn.setOnAction(e -> {
            if (onNavigateVehicles != null) onNavigateVehicles.run();
        });
        navMyBookingsBtn.setOnAction(e -> {
            if (UserSession.getInstance().isLoggedIn()) {
                if (onNavigateMyBookings != null) onNavigateMyBookings.run();
            } else {
                if (onLogin != null) onLogin.run();
            }
        });
        navAboutBtn.setOnAction(e -> {
            if (onNavigateAboutUs != null) {
                onNavigateAboutUs.run();
            } else if (defaultOnNavigateAboutUs != null) {
                defaultOnNavigateAboutUs.run();
            } else {
                InfoDialogs.showAboutUsDialog();
            }
        });
        navContactBtn.setOnAction(e -> {
            if (onNavigateContact != null) {
                onNavigateContact.run();
            } else {
                InfoDialogs.showContactDialog();
            }
        });

        // Initialize auth area
        refreshAuthBox();

        // Listen for session login/logout changes
        UserSession.getInstance().addListener(user -> Platform.runLater(this::refreshAuthBox));
    }

    private Button createNavPill(String text, String svgPath, boolean isActive) {
        Button btn = new Button(text);
        if (svgPath != null) {
            Color iconColor = isActive ? UiTheme.COLOR_WHITE : UiTheme.COLOR_TEXT_DARK;
            btn.setGraphic(UiTheme.createSvgIcon(svgPath, 15, iconColor));
        }
        btn.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.SEMI_BOLD, 13));
        btn.setPadding(new Insets(7, 16, 7, 16));
        btn.setCursor(Cursor.HAND);
        styleNavPill(btn, svgPath, isActive);
        return btn;
    }

    private void styleNavPill(Button btn, String svgPath, boolean isActive) {
        if (isActive) {
            btn.setTextFill(UiTheme.COLOR_WHITE);
            btn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY, 20));
            btn.setBorder(Border.EMPTY);
            if (svgPath != null) {
                btn.setGraphic(UiTheme.createSvgIcon(svgPath, 15, UiTheme.COLOR_WHITE));
            }
            btn.setOnMouseEntered(null);
            btn.setOnMouseExited(null);
        } else {
            btn.setTextFill(UiTheme.COLOR_TEXT_DARK);
            btn.setBackground(UiTheme.roundedBackground(Color.TRANSPARENT, 20));
            btn.setBorder(Border.EMPTY);
            if (svgPath != null) {
                btn.setGraphic(UiTheme.createSvgIcon(svgPath, 15, UiTheme.COLOR_TEXT_DARK));
            }
            btn.setOnMouseEntered(e -> {
                btn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 20));
                btn.setTextFill(UiTheme.COLOR_PRIMARY);
                if (svgPath != null) {
                    btn.setGraphic(UiTheme.createSvgIcon(svgPath, 15, UiTheme.COLOR_PRIMARY));
                }
            });
            btn.setOnMouseExited(e -> {
                btn.setBackground(UiTheme.roundedBackground(Color.TRANSPARENT, 20));
                btn.setTextFill(UiTheme.COLOR_TEXT_DARK);
                if (svgPath != null) {
                    btn.setGraphic(UiTheme.createSvgIcon(svgPath, 15, UiTheme.COLOR_TEXT_DARK));
                }
            });
        }
    }

    public void setActivePage(NavPage page) {
        this.currentPage = page;
        styleNavPill(navHomeBtn, UiTheme.ICON_HOME, page == NavPage.HOME);
        styleNavPill(navVehiclesBtn, UiTheme.ICON_CAR, page == NavPage.VEHICLES);
        styleNavPill(navMyBookingsBtn, UiTheme.ICON_CALENDAR, page == NavPage.MY_BOOKINGS);
        styleNavPill(navAboutBtn, UiTheme.ICON_INFO, page == NavPage.ABOUT);
        styleNavPill(navContactBtn, UiTheme.ICON_PHONE, page == NavPage.CONTACT);
    }

    public void refreshAuthBox() {
        rightActions.getChildren().clear();

        UserSession session = UserSession.getInstance();
        if (session.isLoggedIn()) {
            HBox userChip = new HBox(8);
            userChip.setAlignment(Pos.CENTER_LEFT);
            userChip.setBackground(UiTheme.roundedBackground(Color.TRANSPARENT, 20));
            userChip.setPadding(new Insets(5, 10, 5, 8));
            userChip.setCursor(Cursor.HAND);

            Circle circle = new Circle(15, Color.web("#E2E8F0"));
            javafx.scene.Node userIcon = UiTheme.createSvgIcon(UiTheme.ICON_USER, 14, Color.web("#475569"));
            StackPane avatar = new StackPane(circle, userIcon);

            Label name = new Label(session.getDisplayName());
            name.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 13));
            name.setTextFill(UiTheme.COLOR_NAVY_DARK);

            Label chevron = new Label("▾");
            chevron.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 12));
            chevron.setTextFill(UiTheme.COLOR_TEXT_MUTED);

            userChip.getChildren().addAll(avatar, name, chevron);

            userChip.setOnMouseEntered(e -> userChip.setBackground(UiTheme.roundedBackground(Color.web("#F1F5F9"), 20)));
            userChip.setOnMouseExited(e -> userChip.setBackground(UiTheme.roundedBackground(Color.TRANSPARENT, 20)));

            // Context / dropdown menu containing: My Bookings, Logout
            javafx.scene.control.ContextMenu userMenu = new javafx.scene.control.ContextMenu();
            userMenu.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 4);");

            javafx.scene.control.MenuItem myBookingsItem = new javafx.scene.control.MenuItem("📅  My Bookings");
            myBookingsItem.setStyle("-fx-font-family: 'Segoe UI', Inter, sans-serif; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 16 8 16;");
            myBookingsItem.setOnAction(e -> {
                if (onNavigateMyBookings != null) onNavigateMyBookings.run();
            });

            javafx.scene.control.MenuItem logoutItem = new javafx.scene.control.MenuItem("🚪  Logout");
            logoutItem.setStyle("-fx-font-family: 'Segoe UI', Inter, sans-serif; -fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #EF4444; -fx-padding: 8 16 8 16;");
            logoutItem.setOnAction(e -> {
                session.logout();
                refreshAuthBox();
                if (onLogout != null) onLogout.run();
            });

            userMenu.getItems().addAll(myBookingsItem, new javafx.scene.control.SeparatorMenuItem(), logoutItem);

            userChip.setOnMouseClicked(e -> {
                if (userMenu.isShowing()) {
                    userMenu.hide();
                } else {
                    userMenu.show(userChip, javafx.geometry.Side.BOTTOM, 0, 6);
                }
            });

            rightActions.getChildren().add(userChip);
        } else {
            rightActions.getChildren().addAll(navSearchBtn, navLoginBtn, navRegisterBtn);
        }
    }

    // --- Getters & Setters ---

    public HBox getBrandLogoBox() {
        return brandLogoBox;
    }

    public Button getNavHomeBtn() {
        return navHomeBtn;
    }

    public Button getNavVehiclesBtn() {
        return navVehiclesBtn;
    }

    public Button getNavMyBookingsBtn() {
        return navMyBookingsBtn;
    }

    public Button getNavHowItWorksBtn() {
        return navMyBookingsBtn;
    }

    public Button getNavAboutBtn() {
        return navAboutBtn;
    }

    public Button getNavContactBtn() {
        return navContactBtn;
    }

    public Button getNavSearchBtn() {
        return navSearchBtn;
    }

    public Button getNavLoginBtn() {
        return navLoginBtn;
    }

    public Button getNavRegisterBtn() {
        return navRegisterBtn;
    }

    public void setOnNavigateHome(Runnable onNavigateHome) {
        this.onNavigateHome = onNavigateHome;
    }

    public void setOnNavigateVehicles(Runnable onNavigateVehicles) {
        this.onNavigateVehicles = onNavigateVehicles;
    }

    public void setOnNavigateMyBookings(Runnable onNavigateMyBookings) {
        this.onNavigateMyBookings = onNavigateMyBookings;
    }

    public void setOnNavigateHowItWorks(Runnable onNavigateHowItWorks) {
        this.onNavigateMyBookings = onNavigateHowItWorks;
    }

    public void setOnNavigateAboutUs(Runnable onNavigateAboutUs) {
        this.onNavigateAboutUs = onNavigateAboutUs;
    }

    public void setOnNavigateContact(Runnable onNavigateContact) {
        this.onNavigateContact = onNavigateContact;
    }

    public void setOnSearch(Runnable onSearch) {
        this.onSearch = onSearch;
    }

    public void setOnLogin(Runnable onLogin) {
        this.onLogin = onLogin;
    }

    public void setOnRegister(Runnable onRegister) {
        this.onRegister = onRegister;
    }

    public void setOnOpenDashboard(Runnable onOpenDashboard) {
        this.onNavigateMyBookings = onOpenDashboard;
    }

    public void setOnLogout(Runnable onLogout) {
        this.onLogout = onLogout;
    }
}
