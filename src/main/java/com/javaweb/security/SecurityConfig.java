package com.javaweb.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter authenticationFilter;

    // Kích hoạt Cơ chế băm Mật khẩu 1 chiều tiên tiến BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Bean hỗ trợ kiểm tra Đăng nhập AuthManager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // QUAN TRỌNG NHẤT: Cấu hình quy luật chặn cửa API
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()) // Tắt CSRF vì API hoạt động Stateless
                .authorizeHttpRequests((authorize) ->
                // Cho phép truy cập tự do vào các API auth (Login) và Swagger UI mà không cần
                // Token
                authorize.requestMatchers(
                        org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher("/api/auth/**"),
                        org.springframework.security.web.util.matcher.AntPathRequestMatcher
                                .antMatcher("/v3/api-docs/**"),
                        org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher("/v3/api-docs"),
                        org.springframework.security.web.util.matcher.AntPathRequestMatcher
                                .antMatcher("/swagger-ui/**"),
                        org.springframework.security.web.util.matcher.AntPathRequestMatcher
                                .antMatcher("/swagger-ui.html"),
                        org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher("/error"))
                        .permitAll()
                        .anyRequest().authenticated() // Bắt buộc phải CÓ TOKEN ở tất cả các đường dẫn khác! (VD:
                                                      // /api/users, /api/products)
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Gắn "Ông bảo vệ" chạy đi kiểm tra Token trước mọi bước đăng nhập thông thường
        http.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
