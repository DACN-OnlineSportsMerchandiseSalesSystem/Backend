package com.javaweb.security;

import com.javaweb.entity.User;
import com.javaweb.repository.UserRepository;
import com.javaweb.enums.UserStatus;
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
            String roleName = user.getRole().getName().toUpperCase(); // Đảm bảo luôn viết hoa
            authority = new SimpleGrantedAuthority("ROLE_" + roleName);
        } else {
            authority = new SimpleGrantedAuthority("ROLE_" + AppRoles.CUSTOMER);
        }

        System.out.println(">>> Đã nạp quyền cho " + email + ": " + authority.getAuthority());

        // Kiểm tra trạng thái tài khoản
        boolean enabled = (user.getStatus() == UserStatus.ACTIVE);
        boolean accountNonExpired = true;
        boolean credentialsNonExpired = true;
        boolean accountNonLocked = (user.getStatus() != UserStatus.LOCKED);

        // Nếu user bị BANNED hoặc INACTIVE, Spring Security sẽ tự động ném exception (DisabledException)
        // Nếu user bị LOCKED, Spring Security sẽ ném LockedException

        // Trả về đối tượng UserDetails đặc biệt của Spring với đầy đủ cờ trạng thái
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                enabled,
                accountNonExpired,
                credentialsNonExpired,
                accountNonLocked,
                Collections.singleton(authority)
        );
    }
}
