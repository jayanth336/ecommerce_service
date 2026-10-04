package com.org.ecommerce.common.config;

import com.org.ecommerce.common.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity // this is needed only securityFilterChain
@EnableMethodSecurity // this is to choose which endpoints are allowed by which roles
public class SecurityConfig {
    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    /**
     * In our case, the below method name - passwordEncoder() doesn't matter.
     * You can name it as passwwwwqqordEncoder, don't have to update anywhere and nothing would break.
     *
     * Because Spring considers the method name as Bean name and method type as Bean type.
     * Spring inject the type by default. And the type is PasswordEncoder.
     *
     * The method name matters only when we have multiple Beans of same type.
     * So at that time, we use @Qualifier("method_name").
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // SECURITY_FILTER_CHAIN -> OVERALL FILTER PIPELINE.
    // JWT_FILTER -> ONE OF THE FILTERS WITHIN THE PIPELINE THAT WE ARE SEPARATELY CUSTOMIZING.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        // CSRF only for session-based. Not for stateless like ours.
        httpSecurity.csrf(csrf -> csrf.disable());

        // Server shouldn't store session details.
        httpSecurity.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Configure the endpoints
        httpSecurity.authorizeHttpRequests(auth ->
                auth
                        .requestMatchers(
                        "/auth/login",
                                "/auth/register",
                                "/swagger-ui/**",
                                "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated());

        httpSecurity.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }
}
