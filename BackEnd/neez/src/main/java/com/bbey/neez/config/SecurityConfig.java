package com.bbey.neez.config;

import com.bbey.neez.jwt.JwtAccessDeniedHandler;
import com.bbey.neez.jwt.JwtAuthenticationEntryPoint;
import com.bbey.neez.jwt.JwtAuthenticationFilter;
import com.bbey.neez.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final JwtAuthenticationEntryPoint authenticationEntryPoint;
        private final JwtAccessDeniedHandler accessDeniedHandler;
        private final CustomUserDetailsService userDetailsService;
        private final PasswordEncoder passwordEncoder;

        // AuthenticationManager (스프링 부트 2.7 스타일)
        @Bean
        public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
                AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);

                builder.userDetailsService(userDetailsService)
                                .passwordEncoder(passwordEncoder);

                return builder.build();
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

                http
                                .csrf().disable()
                                .cors().configurationSource(request -> {
                                        org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
                                        config.setAllowedOrigins(Arrays.asList("http://localhost:5177"));
                                        config.setAllowedMethods(Arrays.asList("*"));
                                        config.setAllowedHeaders(Arrays.asList("*"));
                                        config.setAllowCredentials(true);
                                        return config;
                                }) // ← 반드시 추가
                                .and()

                                .sessionManagement()
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                                .and()

                                .exceptionHandling()
                                .authenticationEntryPoint(authenticationEntryPoint)
                                .accessDeniedHandler(accessDeniedHandler)
                                .and()

                                .authorizeRequests()
                                .antMatchers("/api/auth/**").permitAll()
                                .antMatchers(
                                                "/v3/api-docs/**",
                                                "/swagger-ui/**",
                                                "/swagger-ui.html")
                                .permitAll()
                                .antMatchers("/api/admin/**").hasRole("ADMIN")
                                .anyRequest().authenticated()
                                .and()

                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

}
