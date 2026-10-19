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
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
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
