package com.example.shopapp.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    private final Map<String, String> bypassTokens = new HashMap<>();

    public JwtTokenFilter(@Value("${api.prefix}") String apiPrefix) {
        bypassTokens.put("/" + apiPrefix + "/products", "GET");
        bypassTokens.put("/" + apiPrefix + "/categories", "GET");
        bypassTokens.put("/" + apiPrefix + "/users/register", "POST");
        bypassTokens.put("/" + apiPrefix + "/users/login", "POST");
    }


    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();
        String method = request.getMethod();

        for (Map.Entry<String, String> entry : bypassTokens.entrySet()) {
            String bypassPath = entry.getKey();
            String bypassMethod = entry.getValue();

            if (path.startsWith(bypassPath) && method.equalsIgnoreCase(bypassMethod)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        // TODO: Add your JWT token validation logic here

        filterChain.doFilter(request, response); // Continue after token validation
    }
}
