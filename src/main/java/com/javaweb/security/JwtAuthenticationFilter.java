package com.javaweb.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Bước 1: Lấy JWT từ Header của request
        String token = getJWTFromRequest(request);

        // Bước 2: Kiểm tra token hợp lệ
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {

            // Lấy username (email) từ token
            String username = jwtTokenProvider.getUsername(token);

            // Tải lại thông tin người dùng từ CSDL
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

            // Cấp quyền (Tạo Thẻ thông hành Authentication Token)
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Chính thức gán quyền vào Bộ lọc bảo mật của Spring
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        // Tiếp tục màng lọc
        filterChain.doFilter(request, response);
    }

    // Hàm bóc tách chữ Bearer để lấy cái lõi Token
    private String getJWTFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
