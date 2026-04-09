package com.javaweb.security;

import com.javaweb.entity.User;
import com.javaweb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Tìm User bằng Email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy User với email: " + email));

        // Setup phân quyền: Giả sử User.Role.Name cất là "ADMIN", ta gắn thêm chữ "ROLE_" -> "ROLE_ADMIN".
        // Đây là quy chuẩn bắt buộc của Spring Security.
        GrantedAuthority authority;
        if (user.getRole() != null) {
            authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().getName());
        } else {
            authority = new SimpleGrantedAuthority("ROLE_USER"); // Mặc định nếu chưa có quyền
        }

        // Trả về đối tượng UserDetails đặc biệt của Spring
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singleton(authority)
        );
    }
}
