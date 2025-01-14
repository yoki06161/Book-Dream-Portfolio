package com.bookdream.sbb;

import java.io.IOException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class CustomAdminAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    public CustomAdminAuthenticationFilter(AuthenticationManager authenticationManager) {
        super.setAuthenticationManager(authenticationManager);
        super.setFilterProcessesUrl("/admin/login");
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        // 추가 조건 검사 로직을 여기에 추가할 수 있습니다.
        // 예: 특정 IP 주소를 검사하거나 추가 파라미터를 확인
        super.successfulAuthentication(request, response, chain, authResult);
    }
}
