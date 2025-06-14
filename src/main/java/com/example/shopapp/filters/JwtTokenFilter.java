package com.example.shopapp.filters;

import com.example.shopapp.components.JwtTokenUtil;
import com.example.shopapp.security.CustomUserDetails;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;


import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {
    private final UserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;
    private final Multimap<String, String> bypassTokens = ArrayListMultimap.create();
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Value("${api.prefix}")
    private String apiPrefix;

    @PostConstruct
    public void initBypassTokens() {
        bypassTokens.put("/" + apiPrefix + "/products/**", "GET");
        bypassTokens.put("/" + apiPrefix + "/categories", "GET");
        bypassTokens.put("/" + apiPrefix + "/users/**", "POST");
        bypassTokens.put("/" + apiPrefix + "/roles", "GET");
        bypassTokens.put("/" + apiPrefix + "/healthcheck/health", "GET");
        bypassTokens.put("/" + apiPrefix + "/redis/**", "GET");
        bypassTokens.put("/" + apiPrefix + "/redis/**", "POST");
        bypassTokens.put("/" + apiPrefix + "/actuator/**", "GET");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String path = request.getServletPath();
            String method = request.getMethod();

            for (Map.Entry<String, String> entry : bypassTokens.entries()) {
                String bypassPath = entry.getKey();
                String bypassMethod = entry.getValue();

                if (pathMatcher.match(bypassPath, path) && method.equalsIgnoreCase(bypassMethod)) {
                    filterChain.doFilter(request, response);
                    return;
                }
            }

            final String authorizationHeader = request.getHeader("Authorization");

            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
                return;
            }

            String token = authorizationHeader.substring(7);
            String phoneNumber = jwtTokenUtil.getPhoneNumberFromToken(token);

            if (phoneNumber == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                return;
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(phoneNumber);

                if (!jwtTokenUtil.validateToken(token, userDetails)) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                    return;
                }

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

            // Now we can safely continue the request
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            e.printStackTrace(); // for debugging
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: " + e.getMessage());
        }
    }
}