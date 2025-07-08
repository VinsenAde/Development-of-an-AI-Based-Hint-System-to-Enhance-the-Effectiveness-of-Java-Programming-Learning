package com.thesis.java.javalearning.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RoleBasedAuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        boolean isAdmin = authentication.getAuthorities().stream()
          .map(GrantedAuthority::getAuthority)
          .anyMatch("ROLE_ADMIN"::equals);

        if (isAdmin) {
            // send admins to your admin dashboard
            response.sendRedirect("/admin/user-management");
        } else {
            // everyone else to the “student” dashboard
            response.sendRedirect("/dashboard");
        }
    }
}
