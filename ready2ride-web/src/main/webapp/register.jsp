<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <!DOCTYPE html>
    <html lang="en">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Register | Ready2Ride</title>
        <style>
            body {
                font-family: Arial, sans-serif;
                background: #f3f5f9;
                margin: 0;
            }

            .card {
                max-width: 430px;
                margin: 35px auto;
                padding: 30px;
                background: white;
                border-radius: 12px;
                box-shadow: 0 4px 18px #0001;
            }

            h1 {
                color: #172554;
            }

            input {
                box-sizing: border-box;
                width: 100%;
                padding: 12px;
                margin: 8px 0 14px;
                border: 1px solid #ccd2df;
                border-radius: 6px;
            }

            button {
                width: 100%;
                padding: 12px;
                border: 0;
                border-radius: 6px;
                background: #1d4ed8;
                color: white;
                font-weight: bold;
                cursor: pointer;
            }

            .error {
                background: #fee2e2;
                color: #991b1b;
                padding: 10px;
                border-radius: 6px;
            }

            a {
                color: #1d4ed8;
            }
        </style>
    </head>

    <body>
        <div class="card">
            <h1>Create Account</h1>
            <p>Register as a Ready2Ride customer.</p>

            <% if (request.getAttribute("error") !=null) { %>
                <p class="error">
                    <%= request.getAttribute("error") %>
                </p>
                <% } %>

                    <form action="${pageContext.request.contextPath}/register" method="post">
                        <label for="name">Full Name</label>
                        <input type="text" id="name" name="name" maxlength="100" required>

                        <label for="email">Email Address</label>
                        <input type="email" id="email" name="email" autocomplete="email" required>

                        <label for="phone">Phone Number</label>
                        <input type="tel" id="phone" name="phone" autocomplete="tel" required>

                        <label for="drivingLicenseNumber">Driving Licence Number</label>
                        <input type="text" id="drivingLicenseNumber" name="drivingLicenseNumber" maxlength="50">

                        <label for="password">Password (minimum 6 characters)</label>
                        <input type="password" id="password" name="password" minlength="6" autocomplete="new-password"
                            required>

                        <button type="submit">Create Account</button>
                    </form>

                    <p>Already registered?
                        <a href="${pageContext.request.contextPath}/login">Log in</a>
                    </p>
        </div>
    </body>

    </html>