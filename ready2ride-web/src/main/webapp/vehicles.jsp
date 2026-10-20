<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ page import="java.util.List" %>
        <%@ page import="com.ready2ride.core.model.Vehicle" %>

            <!DOCTYPE html>
            <html lang="en">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>Available Vehicles | Ready2Ride</title>

                <style>
                    body {
                        margin: 0;
                        font-family: Arial, sans-serif;
                        background: #f4f7fb;
                        color: #172033;
                    }

                    header {
                        background: #101b35;
                        color: white;
                        padding: 22px 7%;
                    }

                    header a {
                        color: white;
                        text-decoration: none;
                    }

                    main {
                        padding: 35px 7%;
                    }

                    .vehicle-list {
                        display: grid;
                        grid-template-columns: repeat(auto-fit, minmax(230px, 1fr));
                        gap: 20px;
                    }

                    .vehicle {
                        background: white;
                        border-radius: 12px;
                        padding: 24px;
                        box-shadow: 0 5px 18px #17203312;
                    }

                    .vehicle h3 {
                        margin-top: 0;
                    }

                    .type {
                        color: #2864ad;
                        font-weight: bold;
                    }

                    .price {
                        font-size: 22px;
                        font-weight: bold;
                    }

                    .empty {
                        background: white;
                        padding: 25px;
                        border-radius: 10px;
                    }
                </style>
            </head>

            <body>
                <header>
                    <h2><a href="${pageContext.request.contextPath}/">Ready2Ride</a></h2>
                    <p>Explore our available rental vehicles.</p>
                </header>

                <main>
                    <h1>Vehicle Catalogue</h1>

                    <% List<Vehicle> vehicles =
                        (List<Vehicle>) request.getAttribute("vehicles");

                            if (vehicles == null || vehicles.isEmpty()) {
                            %>
                            <div class="empty">
                                <h3>No vehicles available</h3>
                                <p>Please check again later.</p>
                            </div>
                            <% } else { %>
                                <div class="vehicle-list">
                                    <% for (Vehicle vehicle : vehicles) { %>
                                        <article class="vehicle">
                                            <p class="type">
                                                <%= vehicle.getVehicleType() %>
                                            </p>

                                            <h3>
                                                <%= vehicle.getBrand() %>
                                                    <%= vehicle.getModel() %>
                                            </h3>

                                            <p>
                                                Vehicle ID: <%= vehicle.getId() %>
                                            </p>

                                            <p class="price">
                                                ₹<%= vehicle.getRentalRate() %>
                                            </p>

                                            <p>Rental rate according to the configured vehicle policy.</p>
                                        </article>
                                        <% } %>
                                </div>
                                <% } %>
                </main>
            </body>

            </html>