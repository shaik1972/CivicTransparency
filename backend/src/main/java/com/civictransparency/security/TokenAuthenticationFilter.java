package com.civictransparency.security;

import com.civictransparency.auth.service.AccessTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final AccessTokenService accessTokenService;

    public TokenAuthenticationFilter(AccessTokenService accessTokenService) {
        this.accessTokenService = accessTokenService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        return ("POST".equalsIgnoreCase(method) && "/api/v1/auth/login".equals(path))
                || ("POST".equalsIgnoreCase(method) && "/api/v1/auth/refresh".equals(path))
                || ("POST".equalsIgnoreCase(method) && "/api/v1/citizens/register".equals(path))
                || ("GET".equalsIgnoreCase(method) && path.startsWith("/api/v1/categories"))
                || ("GET".equalsIgnoreCase(method) && path.startsWith("/api/v1/geography"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authorization.substring("Bearer ".length()).trim();
            UUID userId = accessTokenService.verify(token);

            List<SimpleGrantedAuthority> authorities = accessTokenService.getAccessRoles(token).stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .toList();

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    authorities);
            authentication.setDetails(request.getRequestURI());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception ignored) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                    "{\"code\":\"INVALID_ACCESS_TOKEN\",\"message\":\"The access token is missing, invalid or expired.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
