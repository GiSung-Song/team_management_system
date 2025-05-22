package com.manager.taskmanager.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("test")
@EnableMethodSecurity
public class TestSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .authorizeHttpRequests((request) -> request
                        .requestMatchers(HttpMethod.POST, "/api/department").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/department/**").hasRole("MANAGER")

                        .requestMatchers(HttpMethod.GET, "/api/department").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/member").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/member").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/member/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/member/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/auth/reIssue").authenticated()
                        .requestMatchers(HttpMethod.POST, "api/auth/logout").authenticated()

                        .requestMatchers(HttpMethod.POST, "/api/member/*/password/reset").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/member/**").hasRole("MANAGER")
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable())
                .httpBasic(http -> http.disable())
                .formLogin(form -> form.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling((exception) ->
                        exception.authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
                        })
                )
                .build();
    }
}
