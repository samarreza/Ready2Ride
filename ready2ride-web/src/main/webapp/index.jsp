<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${appName} | Vehicle Rental</title>
    <style>
        * { box-sizing: border-box; }
        body {
            margin: 0;
            font-family: Arial, sans-serif;
            background: #f4f7fb;
            color: #172033;
        }
        header {
            background: #101b35;
            color: white;
            padding: 20px 7%;
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 15px;
        }
        header h2 { margin: 0; }
        nav a {
            color: white;
            text-decoration: none;
            margin-left: 18px;
        }
        .hero {
            padding: 65px 7%;
            color: white;
            background: linear-gradient(120deg, #14264b, #2864ad);
        }
        .hero h1 { font-size: clamp(32px, 5vw, 52px); margin: 0 0 15px; }
        .hero p { max-width: 650px; line-height: 1.7; font-size: 18px; }
        .section { padding: 35px 7%; }
        .cards {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
        }
        .card {
            background: white;
            padding: 24px;
            border-radius: 12px;
            box-shadow: 0 5px 18px #17203312;
        }
        .vehicle-icon { font-size: 42px; }
        .card p { color: #647087; line-height: 1.6; }
        footer {
            text-align: center;
            padding: 24px;
            background: #101b35;
            color: white;
        }
    </style>
</head>
<body>
<header>
    <h2>🚘 Ready2Ride</h2>
    <nav>
        <a href="${pageContext.request.contextPath}/">Home</a>
        <a href="#vehicles">Vehicles</a>
        <a href="#about">About</a>
    </nav>
</header>

<main>
    <section class="hero">
        <h1>Your Journey, Your Ride.</h1>
        <p>
            Explore vehicle rental options for your next journey.
            Ready2Ride brings vehicle management, reservations,
            customer accounts, and rental services together.
        </p>
    </section>

    <section class="section" id="vehicles">
        <h2>Explore Vehicle Categories</h2>
        <div class="cards">
            <article class="card">
                <div class="vehicle-icon">🚗</div>
                <h3>Cars</h3>
                <p>Comfortable transportation for daily travel and family trips.</p>
            </article>
            <article class="card">
                <div class="vehicle-icon">🏍️</div>
                <h3>Bikes</h3>
                <p>Convenient two-wheelers for city journeys.</p>
            </article>
            <article class="card">
                <div class="vehicle-icon">🚙</div>
                <h3>SUVs</h3>
                <p>Spacious vehicles for group travel and longer journeys.</p>
            </article>
            <article class="card">
                <div class="vehicle-icon">🚐</div>
                <h3>Vans</h3>
                <p>Practical transportation for groups and cargo.</p>
            </article>
        </div>
    </section>

    <section class="section" id="about">
        <h2>About Ready2Ride</h2>
        <p>
            The system is being developed using Java, object-oriented
            programming, PostgreSQL, JDBC, Servlets, JSP, and JavaFX.
        </p>
        <p>
            This initial page presents the interface. Live vehicle
            listings, authentication, and bookings still need to be
            connected to the existing services.
        </p>
    </section>
</main>

<footer>Ready2Ride — Vehicle Rental Management System</footer>
</body>
</html>
