package com.thesis.java.javalearning.config;

import com.thesis.java.javalearning.security.RoleBasedAuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final RoleBasedAuthenticationSuccessHandler successHandler;

    public SecurityConfig(RoleBasedAuthenticationSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Disable CSRF (you’re using stateless/API-style or tokens elsewhere)
            .csrf(csrf -> csrf.disable())

            // 2. Allow H2 console to render frames
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

            // 3. URL authorization rules
            .authorizeHttpRequests(auth -> auth

                // Public assets and pages
                .requestMatchers(
                    "/css/**", "/js/**", "/images/**",
                    "/register", "/login",
                    "/h2-console/**"
                ).permitAll()

                // Open your JSON-based register/login
                .requestMatchers(HttpMethod.POST,
                    "/api/auth/register",
                    "/api/auth/login"
                ).permitAll()

                // Any authenticated user can fetch their own profile
                .requestMatchers("/api/users/me/**").authenticated()

                // --- SUMMARY ACCESS: both USER and ADMIN ---
                .requestMatchers(HttpMethod.GET,
                    "/summary",
                    "/api/admin/users/*/summary"
                ).hasAnyRole("USER", "ADMIN")

                // ADMIN-only APIs beyond summary
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // All other /api/** must be logged in
                .requestMatchers("/api/**").authenticated()

                // Any other MVC page (besides the ones above) requires login
                .anyRequest().authenticated()
            )

            // 4. Form login config
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/api/auth/login")
                .successHandler(successHandler)
                .permitAll()
            )

            // 5. Logout config
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )

            // 6. Session management (stateful for web)
            .sessionManagement(sess ->
                sess.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            );

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
        AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }
}
