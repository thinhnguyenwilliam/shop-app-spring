package com.example.shopapp.configuration;

import com.example.shopapp.filters.JwtTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class WebSecurityConfig {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_USER = "USER";

    private static final String[] PATHS = {
            "/orders/**",
            "/categories/**",
            "/products/**",
            "/order_details/**",
            "/roles/**",
    };

    private final JwtTokenFilter jwtTokenFilter;
    private final AuthenticationProvider authenticationProvider;

    @Value("${api.prefix}")
    private String apiPrefix;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(
                                "http://localhost:4200",
                                "http://localhost:4300",
                                "https://your-production-frontend.com"
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults()) // Enable CORS
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/" + apiPrefix + "/users/login",
                                "/" + apiPrefix + "/users/register",
                                "/" + apiPrefix + "/users/refreshToken",
                                "/" + apiPrefix + "/users/auth/social-login",
                                "/" + apiPrefix + "/users/auth/social/callback",
                                "/" + apiPrefix + "/redis/**",
                                "/" + apiPrefix + "/actuator/**",
                                "/" + apiPrefix + "/comments/**",
                                "/" + apiPrefix + "/coupons/**"
                        ).permitAll()
                        //
                                .requestMatchers(
                                        HttpMethod.GET, "/" + apiPrefix + "/healthcheck/health"
                                ).permitAll()
                        //
                        .requestMatchers(
                                HttpMethod.GET, "/" + apiPrefix + PATHS[1]
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.POST, "/" + apiPrefix + PATHS[1]
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.PUT, "/" + apiPrefix + PATHS[1]
                        ).hasRole(ROLE_ADMIN)
                        .requestMatchers(
                                HttpMethod.DELETE, "/" + apiPrefix + PATHS[1]
                        ).hasRole(ROLE_ADMIN)
                        .requestMatchers(
                                HttpMethod.GET, "/" + apiPrefix + PATHS[2]
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.POST, "/" + apiPrefix + PATHS[2]
                        ).hasRole(ROLE_USER)
                        .requestMatchers(
                                HttpMethod.PUT, "/" + apiPrefix + PATHS[2]
                        ).hasRole(ROLE_USER)
                        .requestMatchers(
                                HttpMethod.DELETE, "/" + apiPrefix + PATHS[2]
                        ).hasRole(ROLE_ADMIN)
                        //
                        .requestMatchers(
                                HttpMethod.GET, "/" + apiPrefix + PATHS[3]
                        ).hasAnyRole(ROLE_ADMIN, ROLE_USER)
                        .requestMatchers(
                                HttpMethod.PUT, "/" + apiPrefix + PATHS[3]
                        ).hasRole(ROLE_ADMIN)
                        .requestMatchers(
                                HttpMethod.POST, "/" + apiPrefix + PATHS[3]
                        ).hasAnyRole(ROLE_USER)
                        .requestMatchers(
                                HttpMethod.DELETE, "/" + apiPrefix + PATHS[3]
                        ).hasRole(ROLE_ADMIN)
                        //
                        .requestMatchers(
                                HttpMethod.GET, "/" + apiPrefix + PATHS[4]
                        ).permitAll()
                        //
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
