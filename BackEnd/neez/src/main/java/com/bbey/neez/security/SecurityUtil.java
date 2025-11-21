package com.bbey.neez.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.bbey.neez.security.UserPrincipal;

public class SecurityUtil {

    /**
     * 현재 로그인한 사용자의 idx 반환
     */
    public static Long getCurrentUserIdx() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof UserPrincipal)) {
            throw new IllegalStateException("UserPrincipal 타입이 아닙니다.");
        }

        UserPrincipal userPrincipal = (UserPrincipal) principal;
        return userPrincipal.getIdx();
    }

    /**
     * 필요하면 전체 UserPrincipal이 필요한 경우용
     */
    public static UserPrincipal getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof UserPrincipal)) {
            throw new IllegalStateException("UserPrincipal 타입이 아닙니다.");
        }

        return (UserPrincipal) principal;
    }
}
