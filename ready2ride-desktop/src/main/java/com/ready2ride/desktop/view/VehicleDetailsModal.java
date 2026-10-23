package com.ready2ride.desktop.view;

import com.ready2ride.core.model.Bike;
import com.ready2ride.core.model.Car;
import com.ready2ride.core.model.SUV;
import com.ready2ride.core.model.Van;
import com.ready2ride.core.model.Vehicle;
import com.ready2ride.core.service.BookingService;
import com.ready2ride.core.service.PaymentService;
import com.ready2ride.desktop.util.UiTheme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Border;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Vehicle Details and Rental Selection Modal matching Screenshot 4 reference.
 * Displays vehicle photo, gallery thumbnails, specifications, date pickers,
 * pickup/return location selectors, real-time total estimate, and direct Book Now button.
 */
public class VehicleDetailsModal {

    private final Dialog<Boolean> dialog;
    private final Vehicle vehicle;
    private final byte[] primaryImageBytes;
    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final Runnable onOpenMyBookings;

    private DatePicker pickupPicker;
    private DatePicker returnPicker;
    private ComboBox<String> pickupLocationCombo;
    private ComboBox<String> returnLocationCombo;
    private Label totalAmountLabel;
    private Label formulaLabel;
    private Label validationErrorLabel;

    public VehicleDetailsModal(Vehicle vehicle, byte[] primaryImageBytes,
                               LocalDate initialPickup, LocalDate initialReturn, String initialLocation,
                               BookingService bookingService, PaymentService paymentService,
                               Runnable onOpenMyBookings) {
        this.vehicle = Objects.requireNonNull(vehicle, "Vehicle must not be null");
        this.primaryImageBytes = primaryImageBytes;
        this.bookingService = Objects.requireNonNull(bookingService, "BookingService must not be null");
        this.paymentService = paymentService;
        this.onOpenMyBookings = onOpenMyBookings;

        dialog = new Dialog<>();
        dialog.setTitle("Vehicle Details — " + vehicle.getBrand() + " " + vehicle.getModel());

        VBox rootBox = new VBox(16);
        rootBox.setPrefWidth(960);
        rootBox.setMinWidth(860);
        rootBox.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 20));
        rootBox.setPadding(new Insets(24, 28, 28, 28));

        // Top bar with close button
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_RIGHT);

        Button closeBtn = new Button("✕");
        closeBtn.setFont(Font.font(16));
        closeBtn.setTextFill(UiTheme.COLOR_TEXT_MUTED);
        closeBtn.setBackground(UiTheme.fillBackground(Color.TRANSPARENT));
        closeBtn.setBorder(Border.EMPTY);
        closeBtn.setCursor(Cursor.HAND);
        closeBtn.setOnAction(e -> dialog.close());
        topBar.getChildren().add(closeBtn);

        // Two Column Layout (Screenshot 4)
        HBox contentCols = new HBox(28);
        contentCols.setAlignment(Pos.TOP_LEFT);

        // Left Column: Big photo + thumbnails
        VBox leftCol = buildLeftGallery();

        // Right Column: Details, Specs, Dates, Locations, Total & Book Now
        VBox rightCol = buildRightDetails(initialPickup, initialReturn, initialLocation);

        contentCols.getChildren().addAll(leftCol, rightCol);
        rootBox.getChildren().addAll(topBar, contentCols);

        dialog.getDialogPane().setContent(rootBox);
        dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);
        dialog.getDialogPane().lookupButton(javafx.scene.control.ButtonType.CLOSE).setVisible(false);
    }

    private VBox buildLeftGallery() {
        VBox box = new VBox(14);
        box.setPrefWidth(460);
        box.setMinWidth(420);

        // Main vehicle photo
        ImageView mainImg = new ImageView();
        mainImg.setPreserveRatio(true);
        mainImg.setFitHeight(260);
        mainImg.setFitWidth(440);

        if (primaryImageBytes != null && primaryImageBytes.length > 0) {
            try {
                mainImg.setImage(new Image(new ByteArrayInputStream(primaryImageBytes)));
            } catch (Exception ignored) {
            }
        }

        StackPane mainWrapper = new StackPane(mainImg);
        mainWrapper.setPrefHeight(270);
        mainWrapper.setAlignment(Pos.CENTER);
        mainWrapper.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_BACKGROUND_LIGHT, 16));
        mainWrapper.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 16, 1));

        // Thumbnail gallery with left/right buttons (Screenshot 4)
        HBox galleryRow = new HBox(10);
        galleryRow.setAlignment(Pos.CENTER);

        Button prevBtn = new Button("‹");
        prevBtn.setFont(Font.font(18));
        prevBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_BACKGROUND_LIGHT, 16));
        prevBtn.setCursor(Cursor.HAND);

        Button nextBtn = new Button("›");
        nextBtn.setFont(Font.font(18));
        nextBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_BACKGROUND_LIGHT, 16));
        nextBtn.setCursor(Cursor.HAND);

        HBox thumbs = new HBox(8);
        thumbs.setAlignment(Pos.CENTER);

        for (int i = 0; i < 4; i++) {
            StackPane thumb = new StackPane();
            thumb.setPrefSize(72, 50);
            thumb.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_BACKGROUND_LIGHT, 8));
            thumb.setBorder(UiTheme.strokeBorder(i == 0 ? UiTheme.COLOR_PRIMARY : UiTheme.COLOR_BORDER_LIGHT, 8, i == 0 ? 1.5 : 1));

            if (primaryImageBytes != null && primaryImageBytes.length > 0) {
                ImageView iv = new ImageView(new Image(new ByteArrayInputStream(primaryImageBytes)));
                iv.setPreserveRatio(true);
                iv.setFitHeight(44);
                iv.setFitWidth(66);
                thumb.getChildren().add(iv);
            }
            thumbs.getChildren().add(thumb);
        }

        galleryRow.getChildren().addAll(prevBtn, thumbs, nextBtn);
        box.getChildren().addAll(mainWrapper, galleryRow);
        return box;
    }

    private VBox buildRightDetails(LocalDate initialPickup, LocalDate initialReturn, String initialLocation) {
        VBox box = new VBox(14);
        box.setPrefWidth(420);
        HBox.setHgrow(box, Priority.ALWAYS);

        // Name + Category Badge
        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(vehicle.getBrand() + " " + vehicle.getModel());
        nameLabel.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 22));
        nameLabel.setTextFill(UiTheme.COLOR_NAVY_DARK);

        Label catBadge = new Label(vehicle.getVehicleType().getDisplayName());
        catBadge.setFont(UiTheme.FONT_SMALL);
        catBadge.setTextFill(UiTheme.COLOR_PRIMARY);
        catBadge.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY_LIGHT, 6));
        catBadge.setPadding(new Insets(4, 10, 4, 10));

        titleRow.getChildren().addAll(nameLabel, catBadge);

        Label subtitle = new Label("Comfort for every journey");
        subtitle.setFont(UiTheme.FONT_BODY);
        subtitle.setTextFill(UiTheme.COLOR_TEXT_MUTED);

        // Specs Row (Screenshot 4: 5 Seats, 2 Bags, Petrol, Automatic)
        HBox specsRow = new HBox(14);
        specsRow.setAlignment(Pos.CENTER_LEFT);

        if (vehicle instanceof Car car) {
            specsRow.getChildren().addAll(
                    createSpecBadge(UiTheme.ICON_USER, car.getSeatingCapacity() + " Seats"),
                    createSpecBadge(UiTheme.ICON_SHIELD, car.getLuggageCapacityBags() + " Bags"),
                    createSpecBadge(UiTheme.ICON_RUPEE, car.getFuelType()),
                    createSpecBadge(UiTheme.ICON_HOW_IT_WORKS, "Automatic")
            );
        } else if (vehicle instanceof Bike bike) {
            specsRow.getChildren().addAll(
                    createSpecBadge(UiTheme.ICON_USER, bike.getSeatingCapacity() + " Seats"),
                    createSpecBadge(UiTheme.ICON_RUPEE, bike.getMileageKmpl() + " kmpl"),
                    createSpecBadge(UiTheme.ICON_HOW_IT_WORKS, "Manual")
            );
        } else if (vehicle instanceof SUV suv) {
            specsRow.getChildren().addAll(
                    createSpecBadge(UiTheme.ICON_USER, suv.getSeatingCapacity() + " Seats"),
                    createSpecBadge(UiTheme.ICON_SHIELD, suv.getLuggageCapacityBags() + " Bags"),
                    createSpecBadge(UiTheme.ICON_RUPEE, suv.getFuelType()),
                    createSpecBadge(UiTheme.ICON_HOW_IT_WORKS, "4WD Automatic")
            );
        } else if (vehicle instanceof Van van) {
            specsRow.getChildren().addAll(
                    createSpecBadge(UiTheme.ICON_USER, van.getSeatingCapacity() + " Seats"),
                    createSpecBadge(UiTheme.ICON_SHIELD, van.getLuggageCapacityBags() + " Bags"),
                    createSpecBadge(UiTheme.ICON_RUPEE, van.getFuelType()),
                    createSpecBadge(UiTheme.ICON_HOW_IT_WORKS, "Manual")
            );
        }

        // Rental Price
        VBox priceBlock = new VBox(2);
        Label pHead = new Label("Rental Price");
        pHead.setFont(UiTheme.FONT_SMALL);
        pHead.setTextFill(UiTheme.COLOR_TEXT_MUTED);

        HBox pRow = new HBox(6);
        pRow.setAlignment(Pos.BASELINE_LEFT);
        Label rateLbl = new Label(String.format("₹%,.0f", vehicle.getRentalRate().doubleValue()));
        rateLbl.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 22));
        rateLbl.setTextFill(UiTheme.COLOR_PRIMARY);
        Label perDay = new Label(" / day");
        perDay.setFont(UiTheme.FONT_BODY);
        perDay.setTextFill(UiTheme.COLOR_TEXT_MUTED);
        pRow.getChildren().addAll(rateLbl, perDay);
        priceBlock.getChildren().addAll(pHead, pRow);

        // Date Selectors
        LocalDate defPickup = initialPickup != null ? initialPickup : LocalDate.now().plusDays(1);
        LocalDate defReturn = initialReturn != null ? initialReturn : LocalDate.now().plusDays(4);

        HBox datesRow = new HBox(12);
        VBox pBox = new VBox(4, new Label("Pickup Date") {{ setFont(UiTheme.FONT_CAPTION); }},
                pickupPicker = new DatePicker(defPickup));
        VBox rBox = new VBox(4, new Label("Return Date") {{ setFont(UiTheme.FONT_CAPTION); }},
                returnPicker = new DatePicker(defReturn));
        HBox.setHgrow(pBox, Priority.ALWAYS);
        HBox.setHgrow(rBox, Priority.ALWAYS);
        pickupPicker.setMaxWidth(Double.MAX_VALUE);
        returnPicker.setMaxWidth(Double.MAX_VALUE);
        datesRow.getChildren().addAll(pBox, rBox);

        // Location Selectors
        VBox locBox = new VBox(8);
        Label pLocLbl = new Label("Pickup Location");
        pLocLbl.setFont(UiTheme.FONT_CAPTION);
        pickupLocationCombo = new ComboBox<>();
        pickupLocationCombo.getItems().addAll(
                "VIT University, Vellore",
                "Katpadi Railway Station, Vellore",
                "Chennai Airport (MAA)",
                "Bangalore Airport (BLR)",
                "Coimbatore Junction"
        );
        pickupLocationCombo.setValue(initialLocation != null ? initialLocation : "VIT University, Vellore");
        pickupLocationCombo.setMaxWidth(Double.MAX_VALUE);

        Label rLocLbl = new Label("Return Location");
        rLocLbl.setFont(UiTheme.FONT_CAPTION);
        returnLocationCombo = new ComboBox<>();
        returnLocationCombo.getItems().addAll(
                "Same as pickup location",
                "VIT University, Vellore",
                "Katpadi Railway Station, Vellore",
                "Chennai Airport (MAA)",
                "Bangalore Airport (BLR)"
        );
        returnLocationCombo.setValue("Same as pickup location");
        returnLocationCombo.setMaxWidth(Double.MAX_VALUE);

        locBox.getChildren().addAll(pLocLbl, pickupLocationCombo, rLocLbl, returnLocationCombo);

        // Estimated Total Container (light blue rounded container, Screenshot 4)
        HBox totalCard = new HBox();
        totalCard.setAlignment(Pos.CENTER_LEFT);
        totalCard.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY_LIGHT, 12));
        totalCard.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_PRIMARY_BORDER, 12, 1));
        totalCard.setPadding(new Insets(12, 16, 12, 16));

        VBox totalLeft = new VBox(2);
        Label estLabel = new Label("Estimated Total");
        estLabel.setFont(UiTheme.FONT_BODY_BOLD);
        estLabel.setTextFill(UiTheme.COLOR_NAVY_DARK);
        formulaLabel = new Label("3 days × ₹ " + vehicle.getRentalRate());
        formulaLabel.setFont(UiTheme.FONT_SMALL);
        formulaLabel.setTextFill(UiTheme.COLOR_TEXT_MUTED);
        totalLeft.getChildren().addAll(estLabel, formulaLabel);

        Region totalSpacer = new Region();
        HBox.setHgrow(totalSpacer, Priority.ALWAYS);

        totalAmountLabel = new Label("₹ 7,497");
        totalAmountLabel.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 22));
        totalAmountLabel.setTextFill(UiTheme.COLOR_PRIMARY);

        totalCard.getChildren().addAll(totalLeft, totalSpacer, totalAmountLabel);

        // Recalculate total on date changes
        Runnable recalcTotal = () -> {
            LocalDate p = pickupPicker.getValue();
            LocalDate r = returnPicker.getValue();
            if (p != null && r != null && r.isAfter(p)) {
                long days = ChronoUnit.DAYS.between(p, r);
                if (days < 1) days = 1;
                BigDecimal total = vehicle.getRentalRate().multiply(BigDecimal.valueOf(days));
                totalAmountLabel.setText(String.format("₹ %,.0f", total.doubleValue()));
                formulaLabel.setText(days + " days × ₹ " + String.format("%,.0f", vehicle.getRentalRate().doubleValue()));
            } else {
                totalAmountLabel.setText("₹ --");
                formulaLabel.setText("Invalid date range");
            }
        };

        pickupPicker.valueProperty().addListener((o, ov, nv) -> recalcTotal.run());
        returnPicker.valueProperty().addListener((o, ov, nv) -> recalcTotal.run());
        recalcTotal.run();

        validationErrorLabel = new Label();
        validationErrorLabel.setFont(UiTheme.FONT_CAPTION);
        validationErrorLabel.setTextFill(UiTheme.COLOR_ERROR);
        validationErrorLabel.setWrapText(true);
        validationErrorLabel.setVisible(false);
        validationErrorLabel.setManaged(false);

        // Book Now Button
        Button bookNowBtn = new Button("📅  Book Now");
        bookNowBtn.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 15));
        bookNowBtn.setTextFill(UiTheme.COLOR_WHITE);
        bookNowBtn.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY, 10));
        bookNowBtn.setBorder(Border.EMPTY);
        bookNowBtn.setMaxWidth(Double.MAX_VALUE);
        bookNowBtn.setPrefHeight(44);
        bookNowBtn.setCursor(Cursor.HAND);

        bookNowBtn.setOnAction(e -> handleBookNow());

        box.getChildren().addAll(
                titleRow, subtitle, specsRow, priceBlock,
                datesRow, locBox, totalCard, validationErrorLabel, bookNowBtn
        );

        return box;
    }

    private void handleBookNow() {
        validationErrorLabel.setVisible(false);
        validationErrorLabel.setManaged(false);

        LocalDate p = pickupPicker.getValue();
        LocalDate r = returnPicker.getValue();

        if (p == null || r == null || !r.isAfter(p)) {
            validationErrorLabel.setText("Return date must be strictly later than pickup date.");
            validationErrorLabel.setVisible(true);
            validationErrorLabel.setManaged(true);
            return;
        }

        LocalDateTime pTime = LocalDateTime.of(p, LocalTime.of(10, 0));
        LocalDateTime rTime = LocalDateTime.of(r, LocalTime.of(10, 0));

        // Recheck vehicle availability
        if (!bookingService.isVehicleAvailable(vehicle.getId(), pTime, rTime)) {
            validationErrorLabel.setText("This vehicle is already reserved for the selected dates. Please choose another date or vehicle.");
            validationErrorLabel.setVisible(true);
            validationErrorLabel.setManaged(true);
            return;
        }

        long days = ChronoUnit.DAYS.between(p, r);
        if (days < 1) days = 1;
        BigDecimal total = vehicle.getRentalRate().multiply(BigDecimal.valueOf(days));

        // Close details modal and transition to Payment Modal (Screenshot 5)
        dialog.close();

        PaymentModal paymentModal = new PaymentModal(
                vehicle,
                primaryImageBytes,
                p,
                r,
                pickupLocationCombo.getValue(),
                returnLocationCombo.getValue(),
                total,
                days,
                bookingService,
                paymentService,
                onOpenMyBookings
        );
        paymentModal.showAndWait();
    }

    private HBox createSpecBadge(String iconSvg, String label) {
        HBox b = new HBox(6);
        b.setAlignment(Pos.CENTER_LEFT);
        b.getChildren().addAll(
                UiTheme.createSvgIcon(iconSvg, 12, UiTheme.COLOR_TEXT_MUTED),
                new Label(label) {{
                    setFont(UiTheme.FONT_SMALL);
                    setTextFill(UiTheme.COLOR_TEXT_MUTED);
                }}
        );
        return b;
    }

    public void showAndWait() {
        dialog.showAndWait();
    }
}
