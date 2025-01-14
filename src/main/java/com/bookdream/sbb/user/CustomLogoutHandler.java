package com.bookdream.sbb.user;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomLogoutHandler implements LogoutHandler {

    private final SecurityContextLogoutHandler securityContextLogoutHandler = new SecurityContextLogoutHandler();

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String uri = request.getRequestURI();

            // 소셜 로그인 로그아웃 로직
            securityContextLogoutHandler.logout(request, response, authentication);
            response.setHeader("Location", "/");
            response.setStatus(HttpServletResponse.SC_FOUND); // 302 Redirect
        }
}