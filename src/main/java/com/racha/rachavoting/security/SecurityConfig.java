package com.racha.rachavoting.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

        // TODO:Rate Limiting for Login Attempts
        // http.addFilterBefore(
        // new RateLimitingFilter("/admin/login", "/candidate/login"),
        // UsernamePasswordAuthenticationFilter.class
        // );

        @Autowired
        private AuthenticationManager authenticationManager;

        // Admin security configuration (higher priority)
        @Order(1)
        @Bean
        public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
                http
                                .securityMatcher("/admin/**") // Only apply to admin paths
                                // Critical security headers
                                .headers(headers -> headers
                                                .contentSecurityPolicy(csp -> csp
                                                                .policyDirectives(
                                                                                "default-src 'self'; script-src 'self'; frame-src 'none';"))
                                                .frameOptions(frame -> frame.deny())
                                                .xssProtection(xss -> xss.disable()))
                                // CSRF configuration
                                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/public/admin/**"))
                                // Authorization rules
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/admin/login").permitAll()
                                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                                .anyRequest().denyAll()) // Deny any non-admin requests that match
                                                                         // /admin/**
                                // Login form
                                .formLogin(form -> form
                                                .loginPage("/admin/login")
                                                .loginProcessingUrl("/admin/login")
                                                .usernameParameter("username")
                                                .passwordParameter("password")
                                                .defaultSuccessUrl("/admin/dashboard", true)
                                                .permitAll())
                                // Logout
                                .logout(logout -> logout
                                                .logoutUrl("/admin/logout")
                                                .logoutRequestMatcher(
                                                                new AntPathRequestMatcher("/admin/logout", "POST"))
                                                .logoutSuccessHandler(adminLogoutSuccessHandler())
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID")
                                                .permitAll())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                                                .sessionFixation().migrateSession()
                                                .maximumSessions(1)
                                                .maxSessionsPreventsLogin(true)
                                                .expiredUrl("/admin/login?expired"))
                                .authenticationManager(authenticationManager);

                return http.build();
        }

        // User security configuration (lower priority)
        @Order(2)
        @Bean
        public SecurityFilterChain userFilterChain(HttpSecurity http) throws Exception {
                http
                                .securityMatcher("/candidate/**", "/") // Apply to user and public paths
                                // Security headers
                                .headers(headers -> headers
                                                .contentSecurityPolicy(csp -> csp
                                                                .policyDirectives(
                                                                                "default-src 'self'; img-src 'self' data:; script-src 'self'; frame-src 'none';"))
                                                .frameOptions(frame -> frame.deny()))
                                // CSRF configuration
                                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/public/candidate/**"))
                                // Authorization rules
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/candidate/login", "/candidate/register", "/")
                                                .permitAll()
                                                .requestMatchers("/candidate/**").hasRole("CANDIDATE")
                                                .requestMatchers("/vote/**").permitAll()
                                                .requestMatchers(
                                                                "/error",
                                                                "/resources/**",
                                                                "/static/**",
                                                                "/css/**",
                                                                "/js/**")
                                                .permitAll()
                                                .anyRequest().authenticated())
                                // Login form
                                .formLogin(form -> form
                                                .loginPage("/candidate/login") // GET - shows the login form
                                                .loginProcessingUrl("/candidate/login") // POST - processes the login
                                                .usernameParameter("email")
                                                .passwordParameter("password")
                                                .defaultSuccessUrl("/candidate/dashboard", true)
                                                .failureUrl("/candidate/login?error=true")
                                                .permitAll())
                                // Logout
                                .logout(logout -> logout
                                                .logoutUrl("/candidate/logout")
                                                .logoutRequestMatcher(
                                                                new AntPathRequestMatcher("/candidate/logout", "POST"))
                                                .logoutSuccessHandler(userLogoutSuccessHandler())
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID")
                                                .permitAll())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                                                .sessionFixation().changeSessionId()
                                                .maximumSessions(1)
                                                .expiredUrl("/candidate/login?expired"))
                                .authenticationManager(authenticationManager);

                return http.build();
        }

        @Bean
        public LogoutSuccessHandler adminLogoutSuccessHandler() {
                return (request, response, authentication) -> {
                        if (authentication != null && authentication.isAuthenticated()) {
                                response.sendRedirect("/admin/login?logout");
                        } else {
                                response.sendRedirect("/");
                        }
                };
        }

        @Bean
        public LogoutSuccessHandler userLogoutSuccessHandler() {
                return (request, response, authentication) -> {
                        if (authentication != null && authentication.isAuthenticated()) {
                                response.sendRedirect("/candidate/login?logout");
                        } else {
                                response.sendRedirect("/");
                        }
                };
        }

        @Bean
        public static PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}