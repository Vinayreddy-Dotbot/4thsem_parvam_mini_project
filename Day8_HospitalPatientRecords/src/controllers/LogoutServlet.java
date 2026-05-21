package controllers;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 🚪 ADMIN LOGOUT CONTROLLER (SERVLET)
 * URL MAP: /logout
 * 🎯 GOAL: Terminate the administrator's authorized session securely.
 */
@SuppressWarnings("serial")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Retrieve the current session if it exists, without creating a new one
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            // Invalidate the session, clearing all security attributes
            session.invalidate();
        }
        
        // Redirect the user back to the admin login terminal
        response.sendRedirect(request.getContextPath() + "/login");
    }
}
