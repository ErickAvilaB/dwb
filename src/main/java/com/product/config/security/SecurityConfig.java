package com.product.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.product.api.entity.AuthAccount;
import com.product.config.jwt.JwtAuthFilter;
import com.product.config.jwt.JwtUtil;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;

@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

    private static final String ADMINISTRATOR = "Administrator";
    private static final String CUSTOMER = "Customer";

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtUtil jwtUtil) {
        AuthenticationEntryPoint unauthorized = (request, response, exception) -> {
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        };

        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(errors -> errors.authenticationEntryPoint(unauthorized))
                .authorizeHttpRequests(requests -> requests
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.POST, "/login", "/user").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/category/active", "/product/{id}", "/product/{id}/image")
                        .hasAnyAuthority(AuthAccount.ADMIN, ADMINISTRATOR, CUSTOMER, AuthAccount.USER)
                        .anyRequest().hasAnyAuthority(AuthAccount.ADMIN, ADMINISTRATOR))
                .addFilterBefore(new JwtAuthFilter(jwtUtil, unauthorized), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
