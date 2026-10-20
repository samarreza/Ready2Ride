
package com.ready2ride.web;

import com.ready2ride.core.database.DatabaseConnectionManager;
import com.ready2ride.core.exception.UserAlreadyExistsException;
import com.ready2ride.core.model.User;
import com.ready2ride.core.repository.RepositoryFactory;
import com.ready2ride.core.service.AuthenticationService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

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
                    "Unable to initialize registration. Check the database configuration.",
                    e
            );
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String fullName = request.getParameter("name");
        String email = request.getParameter("email");
        String phoneNumber = request.getParameter("phone");
        String password = request.getParameter("password");
        String drivingLicenseNumber = request.getParameter("drivingLicenseNumber");

        try {
            User customer = authenticationService.registerCustomer(
                    fullName,
                    email,
                    phoneNumber,
                    password,
                    drivingLicenseNumber
            );

            // Do not automatically sign the new account in.
            // Ask the customer to log in through the normal authentication flow.
            response.sendRedirect(
                    request.getContextPath()
                            + "/login?registered=true"
            );

        } catch (UserAlreadyExistsException e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/register.jsp").forward(request, response);

        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/register.jsp").forward(request, response);
        }
    }
}
