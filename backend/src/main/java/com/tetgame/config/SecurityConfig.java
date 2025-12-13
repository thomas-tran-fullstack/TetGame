package com.tetgame.config;

import com.tetgame.security.JwtAuthenticationFilter;
import com.tetgame.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    public SecurityConfig(CustomUserDetailsService userDetailsService, JwtAuthenticationFilter jwtFilter, OAuth2SuccessHandler oAuth2SuccessHandler) {
        this.userDetailsService = userDetailsService;
        this.jwtFilter = jwtFilter;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/register", "/templates/**", "/api/auth/**", "/oauth2/**", "/oauth2/callback/**", "/actuator/**",
                                        "/swagger-ui/**", "/v3/api-docs/**", "/images/**",
                                        "/static/**", "/index.html", "/*.js", "/*.css", "/css/**", "/js/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2Login(oauth -> oauth.successHandler(oAuth2SuccessHandler))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
