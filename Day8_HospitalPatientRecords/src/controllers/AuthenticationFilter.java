package controllers;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 🛡️ SECURITY INTERCEPTOR FILTER
 * URL MAP: Intercepts all administrative console controllers.
 * 🎯 GOAL: Ensure unauthenticated users are redirected back to the secure /login terminal.
 */
public class AuthenticationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No custom initialization required during container startup
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Extract the target sub-resource path from the request URI
        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
        
        // 🚨 Bypass authentication gate for public login endpoints and static front-end resources
        if (path.startsWith("/login") || path.startsWith("/web/") || path.equals("/index.jsp")) {
            chain.doFilter(request, response);
            return;
        }
        
        // Inspect if an authorized admin session token exists
        HttpSession session = httpRequest.getSession(false);
        boolean loggedIn = (session != null && session.getAttribute("admin_logged_in") != null);
        
        if (loggedIn) {
            // Admin is verified, proceed to load the requested console panel servlet
            chain.doFilter(request, response);
        } else {
            // Security Exception: redirect user straight to the /login workspace
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
        }
    }

    @Override
    public void destroy() {
        // No resource teardown needed during container shutdown
    }
}
