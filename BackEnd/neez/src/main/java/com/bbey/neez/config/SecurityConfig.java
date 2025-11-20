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

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    /**
     * AuthenticationManager 빈 등록
     * - CustomUserDetailsService + PasswordEncoder 사용
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder auth = http.getSharedObject(AuthenticationManagerBuilder.class);

        auth.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder);

        return auth.build();
    }

    /**
     * HTTP 보안 설정
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // CSRF / CORS
                .csrf().disable()
                .cors().and()

                // 세션 사용 X (JWT)
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()

                // 예외 처리 (인증 실패 / 인가 실패)
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
                .and()

                // URL별 권한 정책
                .authorizeRequests()

                // Swagger / OpenAPI 문서 공개
                .antMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/v3/api-docs",
                        "/v3/api-docs.yaml")
                .permitAll()

                // 인증 없이 사용하는 Auth API
                .antMatchers(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/verify",
                        "/api/auth/forgot-password",
                        "/api/auth/reset-password",
                        "/api/auth/refresh")
                .permitAll()

                // Health / DB 체크 (필요 없으면 막아도 됨)
                .antMatchers(
                        "/health",
                        "/health/**",
                        "/db/**")
                .permitAll()

                // 정적 리소스
                .antMatchers(HttpMethod.GET, "/public/**").permitAll()

                // 나머지 모든 요청은 인증 필요
                .anyRequest().authenticated()
                .and()

                // 폼 로그인 / HTTP Basic 사용 안 함
                .formLogin().disable()
                .httpBasic().disable();

        // JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 삽입
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
