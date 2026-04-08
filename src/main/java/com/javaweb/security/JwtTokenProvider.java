package com.javaweb.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // Lấy secret key từ cấu hình application.properties
    @Value("${app.jwt-secret}")
    private String jwtSecret;

    // Lấy thời hạn token từ cấu hình application.properties
    @Value("${app.jwt-expiration-milliseconds}")
    private long jwtExpirationDate;

    // 1. Tạo chuỗi Token lúc đăng nhập
    public String generateToken(Authentication authentication) {
        String username = authentication.getName(); // Mình sẽ dùng Email làm Name

        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + jwtExpirationDate);

        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(expireDate)
                .signWith(key())
                .compact();
    }

    // Thuật toán tạo khóa
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // 2. Mở hộp Token để lấy lại tên Email
    public String getUsername(String token) {
        Claims claims = Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    // 3. Kiểm tra xem Token có phải là hàng dỏm cắt ghép không?
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith((SecretKey) key())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (MalformedJwtException ex) {
            System.out.println("Khóa JWT không đúng định dạng!");
        } catch (ExpiredJwtException ex) {
            System.out.println("Khóa JWT đã hết hạn sử dụng!");
        } catch (UnsupportedJwtException ex) {
            System.out.println("Khóa JWT không được hệ thống hỗ trợ!");
        } catch (IllegalArgumentException ex) {
            System.out.println("Khóa JWT bị rỗng!");
        }
        return false;
    }
}
