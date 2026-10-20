
package com.ready2ride.web;

import com.ready2ride.core.database.DatabaseConnectionManager;
import com.ready2ride.core.model.User;
import com.ready2ride.core.repository.RepositoryFactory;
import com.ready2ride.core.service.AuthenticationService;
import com.ready2ride.core.exception.AuthenticationException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private AuthenticationService authenticationService;

    @Override
    public void init() throws ServletException {
        try {
            DatabaseConnectionManager connectionManager =
                    new DatabaseConnectionManager();

            authenticationService = new AuthenticationService(
                    RepositoryFactory.createJdbcUserRepository(connectionManager)
            );
        } catch (Exception e) {
            throw new ServletException(
                    "Unable to initialize authentication. Check the database configuration.",
                    e
            );
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try {
            User user = authenticationService.authenticate(email, password);

            // Prevent session fixation after successful authentication.
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }

            HttpSession session = request.getSession(true);
            session.setAttribute("authenticatedUser", user);

            // Redirect to the vehicle catalogue after login.
            response.sendRedirect(
                    request.getContextPath() + "/vehicles"
            );

        } catch (AuthenticationException e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/login.jsp").forward(request, response);

        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
}
