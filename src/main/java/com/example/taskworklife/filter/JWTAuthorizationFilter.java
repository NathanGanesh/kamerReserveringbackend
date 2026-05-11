package com.example.taskworklife.filter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.example.taskworklife.util.JWTTokenProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JWTAuthorizationFilter extends OncePerRequestFilter {
    private final JWTTokenProvider jwtTokenProvider;

    public JWTAuthorizationFilter(JWTTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = jwtTokenProvider.resolveToken(request);

        if (StringUtils.isNotBlank(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String subject = jwtTokenProvider.getSubject(token);
                if (jwtTokenProvider.isTokenValid(subject, token)) {
                    Authentication authentication = jwtTokenProvider.getAuthentication(
                            subject,
                            jwtTokenProvider.getAuthorities(token),
                            request
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (JWTVerificationException exception) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
