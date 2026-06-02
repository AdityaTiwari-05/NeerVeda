package com.neerveda.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Separate config for PasswordEncoder to break the circular dependency:
 *   SecurityConfig → UserService (UserDetailsService) → SecurityConfig (PasswordEncoder)
 *
 * By moving PasswordEncoder here, UserService can inject it without
 * touching SecurityConfig at all.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
