package com.bbey.neez.security;

import com.bbey.neez.entity.Auth.Users;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class UserPrincipal implements UserDetails {

    // 🔥 전체 Users 엔티티를 들고 있게 만든다
    private final Users user;

    public UserPrincipal(Users user) {
        this.user = user;
    }

    // 편의 메서드들
    public Long getIdx() {
        return user.getIdx();
    }

    public String getUserId() {
        return user.getUserId();
    }

    public String getRole() {
        return user.getRole(); // role 컬럼 쓰는 경우
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = user.getRole();
        if (role == null || role.isEmpty()) {
            role = "USER";
        }
        // hasRole("ADMIN")을 쓰므로 ROLE_ 접두어 붙여줌
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getUsername() {
        return user.getUserId();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
