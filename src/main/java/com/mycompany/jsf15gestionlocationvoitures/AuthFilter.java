package com.mycompany.jsf15gestionlocationvoitures;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession session = httpRequest.getSession(false);
        String utilisateur = session == null ? null : (String) session.getAttribute(LoginController.SESSION_USER);
        String role = session == null ? null : (String) session.getAttribute(LoginController.SESSION_ROLE);

        boolean espaceClient = httpRequest.getRequestURI().contains("/faces/espace-client/");
        boolean autorise = utilisateur != null
                && (espaceClient ? "CLIENT".equals(role) : "ADMIN".equals(role) || "UTILISATEUR".equals(role));

        if (!autorise) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/faces/login.xhtml");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
