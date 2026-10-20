
package com.ready2ride.web;

import com.ready2ride.core.database.DatabaseConnectionManager;
import com.ready2ride.core.model.Vehicle;
import com.ready2ride.core.repository.RepositoryFactory;
import com.ready2ride.core.service.VehicleService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/vehicles")
public class VehicleCatalogServlet extends HttpServlet {

    private VehicleService vehicleService;

    @Override
    public void init() throws ServletException {
        try {
            DatabaseConnectionManager connectionManager =
                    new DatabaseConnectionManager();

            vehicleService = new VehicleService(
                    RepositoryFactory.createJdbcVehicleRepository(
                            connectionManager
                    )
            );
        } catch (Exception e) {
            throw new ServletException(
                    "Unable to initialize the vehicle catalogue.", e
            );
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        try {
            List<Vehicle> vehicles = vehicleService.getAllVehicles();

            request.setAttribute("vehicles", vehicles);

            request.getRequestDispatcher("/vehicles.jsp")
                   .forward(request, response);

        } catch (RuntimeException e) {
            throw new ServletException(
                    "Unable to load vehicles. Check the database configuration.",
                    e
            );
        }
    }
}
