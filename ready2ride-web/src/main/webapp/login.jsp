<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <!DOCTYPE html>
    <html lang="en">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Login | Ready2Ride</title>
        <style>
            body {
                font-family: Arial, sans-serif;
                background: #f3f5f9;
                margin: 0;
            }

            .card {
                max-width: 400px;
                margin: 70px auto;
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
                margin: 9px 0 16px;
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

            .success {
                background: #dcfce7;
                color: #166534;
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
            <h1>Welcome Back</h1>
            <p>Log in to your Ready2Ride account.</p>

            <% if (request.getAttribute("error") !=null) { %>
                <p class="error">
                    <%= request.getAttribute("error") %>
                </p>
                <% } %>

                    <% if ("true".equals(request.getParameter("registered"))) { %>
                        <p class="success">Registration successful! Please log in.</p>
                        <% } %>

                            <form action="${pageContext.request.contextPath}/login" method="post">
                                <label for="email">Email or User ID</label>
                                <input type="text" id="email" name="email" autocomplete="username" required>

                                <label for="password">Password</label>
                                <input type="password" id="password" name="password" autocomplete="current-password"
                                    required>

                                <button type="submit">Log In</button>
                            </form>

                            <p>Don't have an account?
                                <a href="${pageContext.request.contextPath}/register">Register</a>
                            </p>
                            <p><a href="${pageContext.request.contextPath}/home">Back to Home</a></p>
        </div>
    </body>

    </html>