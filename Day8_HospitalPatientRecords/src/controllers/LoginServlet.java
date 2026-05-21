package controllers;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 🔐 ADMIN LOGIN CONTROLLER (SERVLET)
 * URL MAP: /login
 * GOAL: Authenticate administrators for the Hospital Patient Records console.
 */
@SuppressWarnings("serial")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        
        // If already logged in, redirect straight to dashboard home console
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("admin_logged_in") != null) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }
        
        // Otherwise, render the raw standalone login.html template
        String htmlContent = TemplateEngine.renderRaw(getServletContext(), "login.html");
        
        // Remove any old error placeholders if present
        htmlContent = htmlContent.replace("{{ERROR_MESSAGE}}", "");
        
        response.getWriter().write(htmlContent);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        // High-visibility credentials for classroom instruction: admin / admin123
        if ("admin".equals(username) && "admin123".equals(password)) {
            // Establish security session
            HttpSession session = request.getSession(true);
            session.setAttribute("admin_logged_in", true);
            
            // Redirect to dashboard home console
            response.sendRedirect(request.getContextPath() + "/home");
        } else {
            // Render login screen again with active compiler warning alert banner
            String htmlContent = TemplateEngine.renderRaw(getServletContext(), "login.html");
            
            String alertBox = "<div class=\"alert-box alert-danger\" style=\"margin-top: 1rem;\">" +
                              "<i class=\"fa-solid fa-triangle-exclamation\"></i> " +
                              "<span><strong>AUTH FAIL:</strong> Invalid credentials. Please try again.</span>" +
                              "</div>";
            
            htmlContent = htmlContent.replace("{{ERROR_MESSAGE}}", alertBox);
            response.getWriter().write(htmlContent);
        }
    }
}
