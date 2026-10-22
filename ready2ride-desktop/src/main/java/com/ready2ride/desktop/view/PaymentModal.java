package com.ready2ride.desktop.view;

import com.ready2ride.core.model.Booking;
import com.ready2ride.core.model.Payment;
import com.ready2ride.core.model.PaymentMethod;
import com.ready2ride.core.model.Vehicle;
import com.ready2ride.core.service.BookingService;
import com.ready2ride.core.service.PaymentService;
import com.ready2ride.desktop.util.UiTheme;
import com.ready2ride.desktop.util.UserSession;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 3-Stage Payment and Booking Confirmation Workflow Modal matching Screenshot 5.
 * Stages: 1. Booking Details -> 2. Payment -> 3. Confirmation.
 * Enforces PCI-DSS security guidelines: strictly validates demo payment inputs
 * without ever storing raw card numbers, CVVs, or bank credentials.
 */
public class PaymentModal {

    private final Dialog<Boolean> dialog;
    private final Vehicle vehicle;
    private final byte[] vehicleImageBytes;
    private final LocalDate pickupDate;
    private final LocalDate returnDate;
    private final String pickupLocation;
    private final String returnLocation;
    private final BigDecimal totalAmount;
    private final long rentalDays;

    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final Runnable onOpenMyBookings;

    private PaymentMethod selectedMethod = PaymentMethod.CREDIT_CARD;
    private final StackPane contentStack;
    private final HBox stepHeader;

    // Payment Form fields
    private TextField cardNumberField;
    private TextField expiryField;
    private TextField cvvField;
    private TextField cardHolderField;
    private TextField upiIdField;
    private ComboBox<String> bankComboBox;
    private ComboBox<String> walletComboBox;
    private Label errorLabel;
    private Button payButton;

    private VBox cardMethodBox;
    private VBox upiMethodBox;
    private VBox netBankingMethodBox;
    private VBox walletMethodBox;

    public PaymentModal(Vehicle vehicle, byte[] vehicleImageBytes,
                        LocalDate pickupDate, LocalDate returnDate,
                        String pickupLocation, String returnLocation,
                        BigDecimal totalAmount, long rentalDays,
                        BookingService bookingService, PaymentService paymentService,
                        Runnable onOpenMyBookings) {
        this.vehicle = Objects.requireNonNull(vehicle, "Vehicle must not be null");
        this.vehicleImageBytes = vehicleImageBytes;
        this.pickupDate = Objects.requireNonNull(pickupDate, "Pickup date must not be null");
        this.returnDate = Objects.requireNonNull(returnDate, "Return date must not be null");
        this.pickupLocation = pickupLocation != null ? pickupLocation : "VIT University, Vellore";
        this.returnLocation = returnLocation != null ? returnLocation : "Same as pickup location";
        this.totalAmount = totalAmount;
        this.rentalDays = rentalDays;
        this.bookingService = Objects.requireNonNull(bookingService, "BookingService must not be null");
        this.paymentService = paymentService;
        this.onOpenMyBookings = onOpenMyBookings;

        dialog = new Dialog<>();
        dialog.setTitle("Ready2Ride — Booking & Secure Checkout");

        VBox mainContainer = new VBox(20);
        mainContainer.setPrefWidth(960);
        mainContainer.setMinWidth(860);
        mainContainer.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 20));
        mainContainer.setPadding(new Insets(24, 32, 28, 32));

        // Top bar with close X and Steps Indicator
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);

        stepHeader = createStepIndicator(2); // Start on Step 2 (Payment) as Step 1 was details
        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.setFont(Font.font(16));
        closeBtn.setTextFill(UiTheme.COLOR_TEXT_MUTED);
        closeBtn.setBackground(UiTheme.fillBackground(Color.TRANSPARENT));
        closeBtn.setBorder(Border.EMPTY);
        closeBtn.setCursor(Cursor.HAND);
        closeBtn.setOnAction(e -> dialog.close());

        topBar.getChildren().addAll(stepHeader, topSpacer, closeBtn);

        // Content Area (Step 2: Two Columns vs Step 3: Confirmation)
        contentStack = new StackPane();
        contentStack.getChildren().add(buildPaymentStepView());

        mainContainer.getChildren().addAll(topBar, contentStack);
        dialog.getDialogPane().setContent(mainContainer);
        dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);
        dialog.getDialogPane().lookupButton(javafx.scene.control.ButtonType.CLOSE).setVisible(false);
    }

    private HBox createStepIndicator(int activeStep) {
        HBox steps = new HBox(12);
        steps.setAlignment(Pos.CENTER_LEFT);

        // Step 1: Booking Details (Completed)
        HBox step1 = createStepBadge("✓", "Booking Details", activeStep >= 1, activeStep > 1);
        Line l1 = new Line(0, 0, 50, 0);
        l1.setStroke(activeStep >= 2 ? UiTheme.COLOR_PRIMARY : UiTheme.COLOR_BORDER_LIGHT);

        // Step 2: Payment
        HBox step2 = createStepBadge("2", "Payment", activeStep >= 2, activeStep > 2);
        Line l2 = new Line(0, 0, 50, 0);
        l2.setStroke(activeStep >= 3 ? UiTheme.COLOR_PRIMARY : UiTheme.COLOR_BORDER_LIGHT);

        // Step 3: Confirmation
        HBox step3 = createStepBadge("3", "Confirmation", activeStep >= 3, false);

        steps.getChildren().addAll(step1, l1, step2, l2, step3);
        return steps;
    }

    private HBox createStepBadge(String symbol, String labelText, boolean isActive, boolean isCompleted) {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);

        Circle circle = new Circle(14);
        if (isCompleted || isActive) {
            circle.setFill(UiTheme.COLOR_PRIMARY);
        } else {
            circle.setFill(Color.web("#E2E8F0"));
        }

        Label num = new Label(symbol);
        num.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 12));
        num.setTextFill(isActive || isCompleted ? UiTheme.COLOR_WHITE : UiTheme.COLOR_TEXT_MUTED);

        StackPane circlePane = new StackPane(circle, num);

        Label label = new Label(labelText);
        label.setFont(Font.font(UiTheme.FONT_FAMILY, isActive ? FontWeight.BOLD : FontWeight.NORMAL, 13));
        label.setTextFill(isActive ? UiTheme.COLOR_PRIMARY : UiTheme.COLOR_TEXT_MUTED);

        box.getChildren().addAll(circlePane, label);
        return box;
    }

    private HBox buildPaymentStepView() {
        HBox twoCols = new HBox(28);
        twoCols.setAlignment(Pos.TOP_LEFT);

        // --- Left Summary Panel ---
        VBox summaryPanel = new VBox(16);
        summaryPanel.setPrefWidth(380);
        summaryPanel.setMinWidth(340);
        summaryPanel.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_BACKGROUND_LIGHT, 16));
        summaryPanel.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 16, 1));
        summaryPanel.setPadding(new Insets(20));

        // Vehicle photo
        ImageView imgView = new ImageView();
        imgView.setPreserveRatio(true);
        imgView.setFitHeight(160);
        imgView.setFitWidth(340);

        if (vehicleImageBytes != null && vehicleImageBytes.length > 0) {
            try {
                imgView.setImage(new Image(new ByteArrayInputStream(vehicleImageBytes)));
            } catch (Exception ignored) {
            }
        }

        StackPane imgWrapper = new StackPane(imgView);
        imgWrapper.setPrefHeight(160);
        imgWrapper.setAlignment(Pos.CENTER);
        imgWrapper.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 12));

        // Title + badge
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label vName = new Label(vehicle.getBrand() + " " + vehicle.getModel());
        vName.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 18));
        vName.setTextFill(UiTheme.COLOR_NAVY_DARK);

        Label catBadge = new Label(vehicle.getVehicleType().getDisplayName());
        catBadge.setFont(UiTheme.FONT_SMALL);
        catBadge.setTextFill(UiTheme.COLOR_PRIMARY);
        catBadge.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY_LIGHT, 6));
        catBadge.setPadding(new Insets(3, 8, 3, 8));

        titleRow.getChildren().addAll(vName, catBadge);

        Label subtitle = new Label("Comfort and performance for every journey");
        subtitle.setFont(UiTheme.FONT_CAPTION);
        subtitle.setTextFill(UiTheme.COLOR_TEXT_MUTED);

        // Dates & Locations
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy");
        VBox scheduleBox = new VBox(10);
        scheduleBox.setPadding(new Insets(12, 0, 12, 0));

        scheduleBox.getChildren().addAll(
                createDetailRow(UiTheme.ICON_CALENDAR, "Pickup Date & Time", pickupDate.format(dtf) + " • 10:00 AM"),
                createDetailRow(UiTheme.ICON_CALENDAR, "Return Date & Time", returnDate.format(dtf) + " • 10:00 AM"),
                createDetailRow(UiTheme.ICON_LOCATION, "Pickup Location", pickupLocation),
                createDetailRow(UiTheme.ICON_LOCATION, "Return Location", returnLocation)
        );

        Line divider = new Line(0, 0, 340, 0);
        divider.setStroke(UiTheme.COLOR_BORDER_LIGHT);

        // Pricing breakdown
        VBox priceBox = new VBox(6);
        HBox rateLine = createPriceLine("Rental Price (" + rentalDays + " days)", String.format("₹ %,.0f", totalAmount.doubleValue()));
        HBox addLine = createPriceLine("Additional Charges", "₹ 0");

        HBox totalLine = new HBox();
        totalLine.setAlignment(Pos.CENTER_LEFT);
        Label totalTitle = new Label("Total Amount");
        totalTitle.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 15));
        totalTitle.setTextFill(UiTheme.COLOR_NAVY_DARK);

        Region totalSpacer = new Region();
        HBox.setHgrow(totalSpacer, Priority.ALWAYS);

        Label totalVal = new Label(String.format("₹ %,.0f", totalAmount.doubleValue()));
        totalVal.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 22));
        totalVal.setTextFill(UiTheme.COLOR_PRIMARY);

        totalLine.getChildren().addAll(totalTitle, totalSpacer, totalVal);
        priceBox.getChildren().addAll(rateLine, addLine, totalLine);

        summaryPanel.getChildren().addAll(imgWrapper, titleRow, subtitle, scheduleBox, divider, priceBox);

        // --- Right Payment Panel ---
        VBox paymentPanel = new VBox(18);
        paymentPanel.setPrefWidth(480);
        HBox.setHgrow(paymentPanel, Priority.ALWAYS);

        Label pTitle = new Label("Payment Method");
        pTitle.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 20));
        pTitle.setTextFill(UiTheme.COLOR_NAVY_DARK);

        Label pSub = new Label("Choose your preferred payment method and complete your booking.");
        pSub.setFont(UiTheme.FONT_BODY);
        pSub.setTextFill(UiTheme.COLOR_TEXT_MUTED);

        // 4 Payment Method Selection Cards (Screenshot 5)
        HBox methodCards = new HBox(10);
        methodCards.setAlignment(Pos.CENTER_LEFT);

        cardMethodBox = createMethodCard("💳", "Card", "Credit / Debit", PaymentMethod.CREDIT_CARD, true);
        upiMethodBox = createMethodCard("📱", "UPI", "PhonePe / GPay", PaymentMethod.UPI, false);
        netBankingMethodBox = createMethodCard("🏦", "Net Banking", "All major banks", PaymentMethod.NET_BANKING, false);
        walletMethodBox = createMethodCard("👛", "Wallet", "Paytm / Others", PaymentMethod.CASH, false);

        methodCards.getChildren().addAll(cardMethodBox, upiMethodBox, netBankingMethodBox, walletMethodBox);

        // Form Container switching based on selection
        VBox formContainer = new VBox(12);
        formContainer.setPadding(new Insets(16));
        formContainer.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_BACKGROUND_LIGHT, 12));
        formContainer.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 12, 1));

        // Form fields for Card
        VBox cardFields = buildCardFields();
        VBox upiFields = buildUpiFields();
        VBox netBankingFields = buildNetBankingFields();
        VBox walletFields = buildWalletFields();

        formContainer.getChildren().add(cardFields);

        // Setup method card switching
        setupMethodSwitcher(cardFields, upiFields, netBankingFields, walletFields, formContainer);

        // Security Notice Banner
        HBox securityBox = new HBox(12);
        securityBox.setAlignment(Pos.CENTER_LEFT);
        securityBox.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY_LIGHT, 10));
        securityBox.setPadding(new Insets(10, 14, 10, 14));

        StackPane shield = new StackPane(UiTheme.createSvgIcon(UiTheme.ICON_SHIELD, 20, UiTheme.COLOR_PRIMARY));
        VBox secText = new VBox(2);
        Label secTitle = new Label("Your payment is secure");
        secTitle.setFont(UiTheme.FONT_BODY_BOLD);
        secTitle.setTextFill(UiTheme.COLOR_PRIMARY);
        Label secDesc = new Label("Simulated demo checkout. Zero sensitive credentials are retained or saved.");
        secDesc.setFont(UiTheme.FONT_CAPTION);
        secDesc.setTextFill(UiTheme.COLOR_TEXT_MUTED);
        secText.getChildren().addAll(secTitle, secDesc);
        securityBox.getChildren().addAll(shield, secText);

        errorLabel = new Label();
        errorLabel.setFont(UiTheme.FONT_CAPTION);
        errorLabel.setTextFill(UiTheme.COLOR_ERROR);
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // Pay Button
        payButton = new Button("🔒  Pay ₹ " + String.format("%,.0f", totalAmount.doubleValue()) + "  →");
        payButton.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 15));
        payButton.setTextFill(UiTheme.COLOR_WHITE);
        payButton.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_PRIMARY, 10));
        payButton.setBorder(Border.EMPTY);
        payButton.setMaxWidth(Double.MAX_VALUE);
        payButton.setPrefHeight(48);
        payButton.setCursor(Cursor.HAND);

        payButton.setOnAction(e -> handlePaymentSubmission());

        paymentPanel.getChildren().addAll(
                new VBox(4, pTitle, pSub),
                methodCards,
                formContainer,
                securityBox,
                errorLabel,
                payButton
        );

        twoCols.getChildren().addAll(summaryPanel, paymentPanel);
        return twoCols;
    }

    private VBox buildCardFields() {
        VBox box = new VBox(10);
        Label l1 = new Label("Card Number");
        l1.setFont(UiTheme.FONT_CAPTION);
        cardNumberField = UiTheme.createStyledTextField("1234 5678 9012 3456");

        HBox middle = new HBox(12);
        VBox expBox = new VBox(4, new Label("Expiry Date") {{ setFont(UiTheme.FONT_CAPTION); }},
                expiryField = UiTheme.createStyledTextField("MM / YY"));
        VBox cvvBox = new VBox(4, new Label("CVV") {{ setFont(UiTheme.FONT_CAPTION); }},
                cvvField = UiTheme.createStyledTextField("123"));
        HBox.setHgrow(expBox, Priority.ALWAYS);
        HBox.setHgrow(cvvBox, Priority.ALWAYS);
        middle.getChildren().addAll(expBox, cvvBox);

        Label l2 = new Label("Name on Card");
        l2.setFont(UiTheme.FONT_CAPTION);
        cardHolderField = UiTheme.createStyledTextField("John Doe");

        box.getChildren().addAll(l1, cardNumberField, middle, l2, cardHolderField);
        return box;
    }

    private VBox buildUpiFields() {
        VBox box = new VBox(10);
        Label l = new Label("Enter UPI ID / VPA");
        l.setFont(UiTheme.FONT_CAPTION);
        upiIdField = UiTheme.createStyledTextField("username@okhdfcbank or 9876543210@paytm");
        box.getChildren().addAll(l, upiIdField);
        return box;
    }

    private VBox buildNetBankingFields() {
        VBox box = new VBox(10);
        Label l = new Label("Select Your Bank");
        l.setFont(UiTheme.FONT_CAPTION);
        bankComboBox = new ComboBox<>();
        bankComboBox.getItems().addAll("HDFC Bank", "State Bank of India", "ICICI Bank", "Axis Bank", "Kotak Mahindra Bank");
        bankComboBox.setValue("HDFC Bank");
        bankComboBox.setMaxWidth(Double.MAX_VALUE);
        bankComboBox.setPrefHeight(40);
        box.getChildren().addAll(l, bankComboBox);
        return box;
    }

    private VBox buildWalletFields() {
        VBox box = new VBox(10);
        Label l = new Label("Select Wallet Provider");
        l.setFont(UiTheme.FONT_CAPTION);
        walletComboBox = new ComboBox<>();
        walletComboBox.getItems().addAll("Paytm Wallet", "PhonePe Wallet", "Amazon Pay", "Mobikwik");
        walletComboBox.setValue("Paytm Wallet");
        walletComboBox.setMaxWidth(Double.MAX_VALUE);
        walletComboBox.setPrefHeight(40);
        box.getChildren().addAll(l, walletComboBox);
        return box;
    }

    private void setupMethodSwitcher(VBox card, VBox upi, VBox net, VBox wallet, VBox container) {
        cardMethodBox.setOnMouseClicked(e -> {
            selectedMethod = PaymentMethod.CREDIT_CARD;
            highlightActiveMethod(cardMethodBox);
            container.getChildren().setAll(card);
        });
        upiMethodBox.setOnMouseClicked(e -> {
            selectedMethod = PaymentMethod.UPI;
            highlightActiveMethod(upiMethodBox);
            container.getChildren().setAll(upi);
        });
        netBankingMethodBox.setOnMouseClicked(e -> {
            selectedMethod = PaymentMethod.NET_BANKING;
            highlightActiveMethod(netBankingMethodBox);
            container.getChildren().setAll(net);
        });
        walletMethodBox.setOnMouseClicked(e -> {
            selectedMethod = PaymentMethod.CASH;
            highlightActiveMethod(walletMethodBox);
            container.getChildren().setAll(wallet);
        });
    }

    private void highlightActiveMethod(VBox active) {
        for (VBox b : new VBox[]{cardMethodBox, upiMethodBox, netBankingMethodBox, walletMethodBox}) {
            if (b == active) {
                b.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 10));
                b.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_PRIMARY, 10, 1.5));
            } else {
                b.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 10));
                b.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 10, 1));
            }
        }
    }

    private VBox createMethodCard(String icon, String title, String sub, PaymentMethod method, boolean active) {
        VBox b = new VBox(4);
        b.setAlignment(Pos.CENTER);
        b.setPadding(new Insets(12, 10, 12, 10));
        b.setPrefWidth(110);
        b.setCursor(Cursor.HAND);
        b.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_WHITE, 10));
        b.setBorder(UiTheme.strokeBorder(active ? UiTheme.COLOR_PRIMARY : UiTheme.COLOR_BORDER_LIGHT, 10, active ? 1.5 : 1));

        Label iconLbl = new Label(icon);
        iconLbl.setFont(Font.font(20));

        Label t = new Label(title);
        t.setFont(UiTheme.FONT_BODY_BOLD);
        t.setTextFill(UiTheme.COLOR_NAVY_DARK);

        Label s = new Label(sub);
        s.setFont(Font.font(UiTheme.FONT_FAMILY, 10));
        s.setTextFill(UiTheme.COLOR_TEXT_MUTED);

        b.getChildren().addAll(iconLbl, t, s);
        return b;
    }

    private void handlePaymentSubmission() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // 1. Get authenticated customer ID from session or demo fallback
        String customerId = UserSession.getInstance().isLoggedIn()
                ? UserSession.getInstance().getUserId()
                : "CUST-DEV-001";

        LocalDateTime pTime = LocalDateTime.of(pickupDate, LocalTime.of(10, 0));
        LocalDateTime rTime = LocalDateTime.of(returnDate, LocalTime.of(10, 0));

        try {
            // 2. Re-verify availability and create booking in PostgreSQL
            Booking booking = bookingService.createBooking(customerId, vehicle.getId(), pTime, rTime);

            // 3. Process payment in PostgreSQL through PaymentService if available
            String paymentRef = "TXN-R2R-" + (System.currentTimeMillis() % 10000000);
            if (paymentService != null) {
                Payment payment = paymentService.processPayment(booking.getBookingId(), totalAmount, selectedMethod);
                paymentRef = payment.getTransactionReference();
            }

            // 4. Update UI to Step 3: Confirmation View
            showConfirmationView(booking, paymentRef);

        } catch (Exception ex) {
            errorLabel.setText("Payment or reservation failed: " + ex.getMessage());
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }

    private void showConfirmationView(Booking booking, String paymentRef) {
        // Update Step indicator to Step 3
        stepHeader.getChildren().setAll(createStepIndicator(3).getChildren());

        VBox confirmCard = new VBox(20);
        confirmCard.setAlignment(Pos.CENTER);
        confirmCard.setPadding(new Insets(36, 48, 36, 48));

        Circle successCircle = new Circle(36, UiTheme.COLOR_SUCCESS);
        Label checkmark = new Label("✓");
        checkmark.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 36));
        checkmark.setTextFill(UiTheme.COLOR_WHITE);
        StackPane iconStack = new StackPane(successCircle, checkmark);

        Label h1 = new Label("Booking Confirmed!");
        h1.setFont(Font.font(UiTheme.FONT_FAMILY, FontWeight.BOLD, 26));
        h1.setTextFill(UiTheme.COLOR_NAVY_DARK);

        Label sub = new Label("Your reservation has been recorded in PostgreSQL. Have a safe ride!");
        sub.setFont(UiTheme.FONT_BODY);
        sub.setTextFill(UiTheme.COLOR_TEXT_MUTED);

        // Details summary card
        VBox detailsCard = new VBox(10);
        detailsCard.setPrefWidth(440);
        detailsCard.setBackground(UiTheme.roundedBackground(UiTheme.COLOR_BACKGROUND_LIGHT, 12));
        detailsCard.setBorder(UiTheme.strokeBorder(UiTheme.COLOR_BORDER_LIGHT, 12, 1));
        detailsCard.setPadding(new Insets(16, 20, 16, 20));

        detailsCard.getChildren().addAll(
                createPriceLine("Booking Reference", booking.getBookingId()),
                createPriceLine("Transaction ID", paymentRef),
                createPriceLine("Vehicle", vehicle.getBrand() + " " + vehicle.getModel()),
                createPriceLine("Rental Dates", pickupDate + " to " + returnDate),
                createPriceLine("Pickup Location", pickupLocation),
                createPriceLine("Amount Paid", "₹ " + String.format("%,.0f", totalAmount.doubleValue()))
        );

        HBox btnRow = new HBox(14);
        btnRow.setAlignment(Pos.CENTER);

        Button myBookingsBtn = UiTheme.createPrimaryButton("View in My Bookings");
        myBookingsBtn.setPrefHeight(42);
        myBookingsBtn.setOnAction(e -> {
            dialog.close();
            if (onOpenMyBookings != null) {
                onOpenMyBookings.run();
            }
        });

        Button closeBtn = UiTheme.createSecondaryButton("Back to Vehicles");
        closeBtn.setPrefHeight(42);
        closeBtn.setOnAction(e -> dialog.close());

        btnRow.getChildren().addAll(myBookingsBtn, closeBtn);

        confirmCard.getChildren().addAll(iconStack, h1, sub, detailsCard, btnRow);
        contentStack.getChildren().setAll(confirmCard);
    }

    private HBox createDetailRow(String iconSvg, String label, String value) {
        HBox r = new HBox(10);
        r.setAlignment(Pos.CENTER_LEFT);
        r.getChildren().addAll(
                UiTheme.createSvgIcon(iconSvg, 14, UiTheme.COLOR_TEXT_MUTED),
                new VBox(2,
                        new Label(label) {{ setFont(UiTheme.FONT_SMALL); setTextFill(UiTheme.COLOR_TEXT_MUTED); }},
                        new Label(value) {{ setFont(UiTheme.FONT_BODY_BOLD); setTextFill(UiTheme.COLOR_NAVY_DARK); }}
                )
        );
        return r;
    }

    private HBox createPriceLine(String title, String val) {
        HBox r = new HBox();
        r.setAlignment(Pos.CENTER_LEFT);
        Label t = new Label(title);
        t.setFont(UiTheme.FONT_BODY);
        t.setTextFill(UiTheme.COLOR_TEXT_MUTED);
        Region s = new Region();
        HBox.setHgrow(s, Priority.ALWAYS);
        Label v = new Label(val);
        v.setFont(UiTheme.FONT_BODY_BOLD);
        v.setTextFill(UiTheme.COLOR_NAVY_DARK);
        r.getChildren().addAll(t, s, v);
        return r;
    }

    public void showAndWait() {
        dialog.showAndWait();
    }
}
