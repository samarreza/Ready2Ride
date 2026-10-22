package com.ready2ride.desktop.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

/**
 * Centralized theme provider for the Ready2Ride JavaFX desktop application.
 * Strictly implements all visual styling through JavaFX classes and properties
 * without any CSS files or setStyle() CSS strings.
 */
public final class UiTheme {

    // --- Color Palette ---
    public static final Color COLOR_PRIMARY = Color.web("#2563EB");       // Vibrant blue accent
    public static final Color COLOR_PRIMARY_HOVER = Color.web("#1D4ED8"); // Darker blue on hover
    public static final Color COLOR_PRIMARY_LIGHT = Color.web("#EFF6FF"); // Subtle light blue
    public static final Color COLOR_PRIMARY_BORDER = Color.web("#BFDBFE");// Light blue border

    public static final Color COLOR_NAVY_DARK = Color.web("#0F172A");     // Deep slate/navy background
    public static final Color COLOR_NAVY_PANEL = Color.web("#1E293B");    // Dark navy card
    public static final Color COLOR_NAVY_HOVER = Color.web("#334155");    // Dark button hover

    public static final Color COLOR_WHITE = Color.WHITE;
    public static final Color COLOR_BACKGROUND_LIGHT = Color.web("#F8FAFC"); // Clean light background
    public static final Color COLOR_BORDER_LIGHT = Color.web("#E2E8F0");     // Soft divider
    public static final Color COLOR_BORDER_INPUT = Color.web("#CBD5E1");     // Input control border
    public static final Color COLOR_BORDER_FOCUS = Color.web("#3B82F6");     // Input focus border

    public static final Color COLOR_TEXT_DARK = Color.web("#0F172A");     // High-contrast headings
    public static final Color COLOR_TEXT_MUTED = Color.web("#64748B");    // Secondary labels / subtitles
    public static final Color COLOR_TEXT_LIGHT = Color.web("#F1F5F9");    // Light text on dark navy
    public static final Color COLOR_TEXT_HINT = Color.web("#94A3B8");     // Footnotes and placeholders

    public static final Color COLOR_SUCCESS = Color.web("#10B981");       // Available green
    public static final Color COLOR_SUCCESS_LIGHT = Color.web("#ECFDF5");
    public static final Color COLOR_WARNING = Color.web("#F59E0B");       // Pending/warning amber
    public static final Color COLOR_WARNING_LIGHT = Color.web("#FFFBEB");
    public static final Color COLOR_ERROR = Color.web("#EF4444");         // Validation error red
    public static final Color COLOR_ERROR_LIGHT = Color.web("#FEF2F2");

    // --- Typography (Clean Segoe UI / System typography) ---
    public static final String FONT_FAMILY = "Segoe UI";

    public static final Font FONT_HERO_TITLE = Font.font(FONT_FAMILY, FontWeight.BOLD, 42);
    public static final Font FONT_HERO_SUBTITLE = Font.font(FONT_FAMILY, FontWeight.NORMAL, 16);
    public static final Font FONT_SECTION_TITLE = Font.font(FONT_FAMILY, FontWeight.BOLD, 26);
    public static final Font FONT_SECTION_SUB = Font.font(FONT_FAMILY, FontWeight.NORMAL, 14);
    public static final Font FONT_CARD_TITLE = Font.font(FONT_FAMILY, FontWeight.BOLD, 18);
    public static final Font FONT_PRICE_BADGE = Font.font(FONT_FAMILY, FontWeight.BOLD, 13);

    public static final Font FONT_TITLE = Font.font(FONT_FAMILY, FontWeight.BOLD, 24);
    public static final Font FONT_HEADER = Font.font(FONT_FAMILY, FontWeight.BOLD, 18);
    public static final Font FONT_SUBHEADER = Font.font(FONT_FAMILY, FontWeight.SEMI_BOLD, 15);
    public static final Font FONT_BODY_BOLD = Font.font(FONT_FAMILY, FontWeight.BOLD, 13);
    public static final Font FONT_BODY = Font.font(FONT_FAMILY, FontWeight.NORMAL, 13);
    public static final Font FONT_CAPTION = Font.font(FONT_FAMILY, FontWeight.NORMAL, 12);
    public static final Font FONT_SMALL = Font.font(FONT_FAMILY, FontWeight.NORMAL, 11);
    public static final Font FONT_SMALL_ITALIC = Font.font(FONT_FAMILY, FontPosture.ITALIC, 11);

    private UiTheme() {
        // Prevent instantiation
    }

    // --- Background & Border Helpers ---

    public static Background fillBackground(Color color) {
        return new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY));
    }

    public static Background roundedBackground(Color color, double radius) {
        return new Background(new BackgroundFill(color, new CornerRadii(radius), Insets.EMPTY));
    }

    public static Border strokeBorder(Color color, double radius, double width) {
        return new Border(new BorderStroke(color, BorderStrokeStyle.SOLID, new CornerRadii(radius), new BorderWidths(width)));
    }

    public static DropShadow cardShadow() {
        return new DropShadow(18, 0, 8, Color.rgb(0, 0, 0, 0.12));
    }

    public static DropShadow subtleShadow() {
        return new DropShadow(8, 0, 3, Color.rgb(0, 0, 0, 0.05));
    }

    // --- Control Factories ---

    public static Button createPrimaryButton(String text) {
        Button btn = new Button(text);
        btn.setFont(FONT_BODY_BOLD);
        btn.setTextFill(COLOR_WHITE);
        btn.setBackground(roundedBackground(COLOR_PRIMARY, 8));
        btn.setBorder(Border.EMPTY);
        btn.setPadding(new Insets(10, 20, 10, 20));
        btn.setCursor(Cursor.HAND);

        btn.setOnMouseEntered(e -> btn.setBackground(roundedBackground(COLOR_PRIMARY_HOVER, 8)));
        btn.setOnMouseExited(e -> btn.setBackground(roundedBackground(COLOR_PRIMARY, 8)));
        return btn;
    }

    public static Button createSecondaryButton(String text) {
        Button btn = new Button(text);
        btn.setFont(FONT_BODY_BOLD);
        btn.setTextFill(COLOR_TEXT_DARK);
        btn.setBackground(roundedBackground(COLOR_BACKGROUND_LIGHT, 8));
        btn.setBorder(strokeBorder(COLOR_BORDER_INPUT, 8, 1));
        btn.setPadding(new Insets(10, 20, 10, 20));
        btn.setCursor(Cursor.HAND);

        btn.setOnMouseEntered(e -> {
            btn.setBackground(roundedBackground(COLOR_BORDER_LIGHT, 8));
            btn.setBorder(strokeBorder(COLOR_PRIMARY, 8, 1));
        });
        btn.setOnMouseExited(e -> {
            btn.setBackground(roundedBackground(COLOR_BACKGROUND_LIGHT, 8));
            btn.setBorder(strokeBorder(COLOR_BORDER_INPUT, 8, 1));
        });
        return btn;
    }

    public static Button createGhostButton(String text) {
        Button btn = new Button(text);
        btn.setFont(FONT_BODY);
        btn.setTextFill(COLOR_PRIMARY);
        btn.setBackground(roundedBackground(Color.TRANSPARENT, 8));
        btn.setBorder(javafx.scene.layout.Border.EMPTY);
        btn.setPadding(new Insets(8, 16, 8, 16));
        btn.setCursor(Cursor.HAND);

        btn.setOnMouseEntered(e -> btn.setBackground(roundedBackground(COLOR_PRIMARY_LIGHT, 8)));
        btn.setOnMouseExited(e -> btn.setBackground(roundedBackground(Color.TRANSPARENT, 8)));
        return btn;
    }

    public static TextField createStyledTextField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setFont(FONT_BODY);
        tf.setPrefHeight(42);
        tf.setBackground(roundedBackground(COLOR_WHITE, 8));
        tf.setBorder(strokeBorder(COLOR_BORDER_INPUT, 8, 1));
        tf.setPadding(new Insets(8, 12, 8, 12));

        tf.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                tf.setBorder(strokeBorder(COLOR_BORDER_FOCUS, 8, 1.5));
                tf.setBackground(roundedBackground(COLOR_WHITE, 8));
            } else {
                tf.setBorder(strokeBorder(COLOR_BORDER_INPUT, 8, 1));
            }
        });
        return tf;
    }

    public static PasswordField createStyledPasswordField(String prompt) {
        PasswordField pf = new PasswordField();
        pf.setPromptText(prompt);
        pf.setFont(FONT_BODY);
        pf.setPrefHeight(42);
        pf.setBackground(roundedBackground(COLOR_WHITE, 8));
        pf.setBorder(strokeBorder(COLOR_BORDER_INPUT, 8, 1));
        pf.setPadding(new Insets(8, 12, 8, 12));

        pf.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                pf.setBorder(strokeBorder(COLOR_BORDER_FOCUS, 8, 1.5));
            } else {
                pf.setBorder(strokeBorder(COLOR_BORDER_INPUT, 8, 1));
            }
        });
        return pf;
    }

    /**
     * Creates a geometric brand logo badge composed entirely of JavaFX shapes
     * inspired by the rounded modern app icons.
     */
    public static StackPane createBrandLogo(double size) {
        StackPane badge = new StackPane();
        badge.setPrefSize(size, size);
        badge.setMaxSize(size, size);

        // Rounded square container in primary blue
        Rectangle background = new Rectangle(size, size);
        background.setArcWidth(size * 0.4);
        background.setArcHeight(size * 0.4);
        background.setFill(COLOR_PRIMARY);

        // Internal 4-dot / grid geometric emblem
        double dotRadius = size * 0.11;
        double offset = size * 0.20;

        Circle dotTopLeft = new Circle(dotRadius, Color.WHITE);
        dotTopLeft.setTranslateX(-offset);
        dotTopLeft.setTranslateY(-offset);

        Circle dotTopRight = new Circle(dotRadius, Color.WHITE);
        dotTopRight.setTranslateX(offset);
        dotTopRight.setTranslateY(-offset);

        Circle dotBottomLeft = new Circle(dotRadius, Color.WHITE);
        dotBottomLeft.setTranslateX(-offset);
        dotBottomLeft.setTranslateY(offset);

        Circle dotBottomRight = new Circle(dotRadius, Color.web("#93C5FD")); // Lighter accent dot
        dotBottomRight.setTranslateX(offset);
        dotBottomRight.setTranslateY(offset);

        badge.getChildren().addAll(background, dotTopLeft, dotTopRight, dotBottomLeft, dotBottomRight);
        return badge;
    }

    // --- SVG Icon Path Constants ---
    public static final String ICON_HOME = "M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z";
    public static final String ICON_CAR = "M18.92 6.01C18.72 5.42 18.16 5 17.5 5h-11c-.66 0-1.21.42-1.42 1.01L3 12v8c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h12v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-8l-2.08-5.99zM6.5 16c-.83 0-1.5-.67-1.5-1.5S5.67 13 6.5 13s1.5.67 1.5 1.5S7.33 16 6.5 16zm11 0c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zM5 11l1.5-4.5h11L19 11H5z";
    public static final String ICON_HOW_IT_WORKS = "M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z";
    public static final String ICON_INFO = "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z";
    public static final String ICON_PHONE = "M6.62 10.79c1.44 2.83 3.76 5.14 6.59 6.59l2.2-2.2c.27-.27.67-.36 1.02-.24 1.12.37 2.33.57 3.57.57.55 0 1 .45 1 1V20c0 .55-.45 1-1 1-9.39 0-17-7.61-17-17 0-.55.45-1 1-1h3.5c.55 0 1 .45 1 1 0 1.25.2 2.45.57 3.57.11.35.03.74-.25 1.02l-2.2 2.2z";
    public static final String ICON_SEARCH = "M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z";
    public static final String ICON_USER = "M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z";
    public static final String ICON_USER_PLUS = "M15 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4zm5-6h-2V6h-2v2h-2v2h2v2h2v-2h2z";
    public static final String ICON_LOCATION = "M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z";
    public static final String ICON_CALENDAR = "M19 3h-1V1h-2v2H8V1H6v2H5c-1.11 0-1.99.9-1.99 2L3 19c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V8h14v11z";
    public static final String ICON_SHIELD = "M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm-2 16l-4-4 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z";
    public static final String ICON_RUPEE = "M13.66 7C13.1 5.82 11.9 5 10 5H6V3h12V1H6c-1.1 0-2 .9-2 2v2c0 1.1.9 2 2 2h4c1.66 0 3 1.34 3 3s-1.34 3-3 3H6v2h5.5l5.5 7h3l-5.84-7.42C16.14 13.9 17 12.1 17 10c0-1.3-.4-2.43-1.1-3.34L18 3h-2.14l-2.2 4z";
    public static final String ICON_HEADSET = "M12 1a9 9 0 0 0-9 9v7c0 1.66 1.34 3 3 3h3v-8H5v-2c0-3.87 3.13-7 7-7s7 3.13 7 7v2h-4v8h3c1.66 0 3-1.34 3-3v-7a9 9 0 0 0-9-9z";
    public static final String ICON_STEERING = "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8 0-.55.06-1.08.17-1.6L7 11.8v.7c0 1.65 1.35 3 3 3h4c1.65 0 3-1.35 3-3v-.7l2.83-1.4c.11.52.17 1.05.17 1.6 0 4.41-3.59 8-8 8zm-2-9c-.55 0-1-.45-1-1s.45-1 1-1h4c.55 0 1 .45 1 1s-.45 1-1 1h-4z";
    public static final String ICON_ARROW_RIGHT = "M12 4l-1.41 1.41L16.17 11H4v2h12.17l-5.58 5.59L12 20l8-8z";
    public static final String ICON_SEATS = "M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z";
    public static final String ICON_BAGS = "M20 6h-4V4c0-1.11-.89-2-2-2h-4c-1.11 0-2 .89-2 2v2H4c-1.11 0-1.99.89-1.99 2L2 19c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V8c0-1.11-.89-2-2-2zm-6 0h-4V4h4v2z";
    public static final String ICON_FUEL = "M19.77 7.23l.01-.01-3.72-3.72L15 4.56l2.11 2.11c-.94.36-1.61 1.26-1.61 2.33 0 1.38 1.12 2.5 2.5 2.5.36 0 .69-.08 1-.21v7.21c0 .55-.45 1-1 1s-1-.45-1-1V14c0-1.1-.9-2-2-2h-1V5c0-1.1-.9-2-2-2H6c-1.1 0-2 .9-2 2v16h10v-7.5h1.5v5c0 1.38 1.12 2.5 2.5 2.5s2.5-1.12 2.5-2.5V9c0-.69-.28-1.32-.73-1.77zM12 10H6V5h6v5zm6 0c-.55 0-1-.45-1-1s.45-1 1-1 1 .45 1 1-.45 1-1 1z";
    public static final String ICON_SPEED = "M20.38 8.57l-1.23 1.85a8 8 0 0 1-.22 7.58H5.07A8 8 0 0 1 15.58 6.85l1.85-1.23A10 10 0 0 0 3.35 19a2 2 0 0 0 1.72 1h13.85a2 2 0 0 0 1.74-1 10 10 0 0 0-.28-10.43zM10.59 15.41a2 2 0 1 1 2.83-2.83l2.83-5.66-5.66 2.83a2 2 0 0 1 0 5.66z";

    /**
     * Creates a scalable vector icon node from SVG path data.
     */
    public static javafx.scene.Node createSvgIcon(String pathData, double size, Color fill) {
        javafx.scene.shape.SVGPath path = new javafx.scene.shape.SVGPath();
        path.setContent(pathData);
        path.setFill(fill);

        double boundsWidth = path.getBoundsInLocal().getWidth();
        double boundsHeight = path.getBoundsInLocal().getHeight();
        double maxDim = Math.max(boundsWidth, boundsHeight);
        if (maxDim > 0) {
            double scale = size / maxDim;
            path.setScaleX(scale);
            path.setScaleY(scale);
        }

        StackPane container = new StackPane(path);
        container.setMinSize(size, size);
        container.setPrefSize(size, size);
        container.setMaxSize(size, size);
        return container;
    }

    /**
     * Creates the Ready2Ride header logo with stylized blue car silhouette and motion speed lines.
     */
    public static javafx.scene.layout.HBox createBrandHeaderLogo() {
        // Stylized vector car icon in bright blue
        javafx.scene.shape.SVGPath carPath = new javafx.scene.shape.SVGPath();
        carPath.setContent("M 0 16 L 8 16 M 2 12 L 12 12 M 0 20 L 10 20 M 14 10 Q 22 2 34 2 L 44 2 Q 52 2 58 10 L 68 12 Q 74 13 76 17 L 76 22 Q 76 24 73 24 L 71 24 C 71 27 68 30 64 30 C 60 30 57 27 57 24 L 31 24 C 31 27 28 30 24 30 C 20 30 17 27 17 24 L 14 24 Q 12 24 12 21 L 12 14 Z M 32 6 L 22 13 L 42 13 L 42 6 Z M 46 6 L 46 13 L 58 13 L 53 7 Q 50 6 46 6 Z");
        carPath.setFill(COLOR_PRIMARY);
        carPath.setScaleX(0.75);
        carPath.setScaleY(0.75);

        StackPane carContainer = new StackPane(carPath);
        carContainer.setPrefSize(64, 36);
        carContainer.setAlignment(Pos.CENTER);

        Label brandTitle = new Label("Ready2Ride");
        brandTitle.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 22));
        brandTitle.setTextFill(COLOR_NAVY_DARK);

        Label tagline = new Label("RENT • RIDE • EXPLORE");
        tagline.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 9));
        tagline.setTextFill(COLOR_PRIMARY);

        VBox textBox = new VBox(0, brandTitle, tagline);
        textBox.setAlignment(Pos.CENTER_LEFT);

        javafx.scene.layout.HBox brandRow = new javafx.scene.layout.HBox(8, carContainer, textBox);
        brandRow.setAlignment(Pos.CENTER_LEFT);
        brandRow.setCursor(Cursor.HAND);
        return brandRow;
    }

    /**
     * Creates a badge pill (e.g. "Available", "Booked") with background and border.
     */
    public static Label createStatusBadge(String text, Color textColor, Color bgColor, Color borderColor) {
        Label badge = new Label(text);
        badge.setFont(FONT_SMALL);
        badge.setTextFill(textColor);
        badge.setBackground(roundedBackground(bgColor, 12));
        badge.setBorder(strokeBorder(borderColor, 12, 1));
        badge.setPadding(new Insets(3, 9, 3, 9));
        return badge;
    }
}
