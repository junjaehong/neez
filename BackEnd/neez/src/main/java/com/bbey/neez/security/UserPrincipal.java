package com.bbey.neez.security;

import com.bbey.neez.entity.Users; // 또는 User 엔티티 이름에 맞게
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;   // 👈 이거 추가!

public class UserPrincipal implements UserDetails {

    private final Users user;  // 엔티티 이름 맞춰서

    public UserPrincipal(Users user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Java 8에서는 List.of 대신 이거 사용
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();   // 로그인 ID가 이메일이면 유지
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() {
        // 삭제 플래그 없으면 true 리턴
        return true;
        // return !user.isDeleted();
    }

    public Users getUser() {
        return user;
    }
}
