package com.bbey.neez.service.Auth;

import com.bbey.neez.DTO.auth.*;
import com.bbey.neez.entity.Company;
import com.bbey.neez.entity.Auth.EmailVerificationToken;
import com.bbey.neez.entity.Auth.Users;
import com.bbey.neez.jwt.JwtUtil;
import com.bbey.neez.repository.CompanyRepository;
import com.bbey.neez.repository.Auth.EmailVerificationTokenRepository;
import com.bbey.neez.repository.Auth.UserRepository;
import com.bbey.neez.security.UserPrincipal;
import com.bbey.neez.entity.Company;
import com.bbey.neez.service.Company.CompanyInfoExtractService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final CompanyInfoExtractService companyInfoExtractService;

    // 기존 코드에서 빠져 있어서 추가
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final PasswordResetService passwordResetService;
    private final CompanyRepository companyRepository;

    // --------------------------------------------------------------------
    // 1. 회원가입: Users에 바로 저장하지 않고, EmailVerificationToken에만 저장
    // --------------------------------------------------------------------
    @Override
    public AuthResponse register(RegisterRequest req) {

        // 1) 이미 가입된 유저인지 체크
        if (userRepository.findByUserId(req.getUserId()).isPresent()) {
            return new AuthResponse(false, "이미 사용 중인 아이디입니다.", null);
        }
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            return new AuthResponse(false, "이미 사용 중인 이메일입니다.", null);
        }

        // 2) 기존에 같은 이메일/아이디로 아직 인증 안된 토큰이 있으면 삭제 (정리용)
        emailVerificationTokenRepository.findAll().stream()
                .filter(t -> t.getEmail().equals(req.getEmail()) || t.getUserId().equals(req.getUserId()))
                .forEach(t -> emailVerificationTokenRepository.deleteById(t.getId()));

        // 3) 토큰 생성
        String token = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(req.getPassword());

        EmailVerificationToken evt = new EmailVerificationToken();
        evt.setToken(token);
        evt.setUserId(req.getUserId());
        evt.setPassword(encodedPassword);
        evt.setName(req.getName());
        evt.setEmail(req.getEmail());
        // 🔥 전화번호/회사정보는 회원가입 단계에서 받지 않는다.
        // evt.setPhone(...) 등 아무것도 안 넣음
        evt.setExpiresAt(LocalDateTime.now().plusMinutes(30)); // 30분 유효

        emailVerificationTokenRepository.save(evt);

        // 4) 인증 메일 발송
        emailService.sendVerificationEmail(req.getEmail(), token);

        // 5) 응답
        return new AuthResponse(true, "회원가입이 접수되었습니다. 이메일을 확인해 주세요.", null);
    }

    // ===============================
    // 로그인
    // ===============================
    @Override
    public AuthResponse login(LoginRequest req) {

        // Spring Security가 비밀번호 인증
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        req.getUserId(),
                        req.getPassword()));

        // 인증 성공 → UserPrincipal 반환됨
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Users user = principal.getUser();

        if (!user.isVerified()) {
            return new AuthResponse(false, "이메일 인증을 완료해주세요.");
        }

        // JWT 생성
        String access = jwtUtil.createAccessToken(user.getUserId());
        String refresh = jwtUtil.createRefreshToken(user.getUserId());

        user.setRefreshToken(refresh);
        userRepository.save(user);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", access);
        tokens.put("refreshToken", refresh);

        return new AuthResponse(true, "로그인 성공", tokens);
    }

    // ===============================
    // 로그아웃
    // ===============================
    @Override
    public AuthResponse logoutByIdx(Long idx) {
        Optional<Users> userOpt = userRepository.findById(idx);
        if (!userOpt.isPresent()) {
            return new AuthResponse(false, "존재하지 않는 유저입니다.");
        }
        Users user = userOpt.get();
        user.setRefreshToken(null);
        userRepository.save(user);
        return new AuthResponse(true, "로그아웃 완료");
    }

    // ===============================
    // 회원 탈퇴
    // ===============================
    @Override
    public AuthResponse delete(DeleteRequest req) {

        Optional<Users> userOpt = userRepository.findByUserId(req.getUserId());
        if (!userOpt.isPresent()) {
            return new AuthResponse(false, "존재하지 않는 유저입니다.");
        }

        Users user = userOpt.get();

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return new AuthResponse(false, "비밀번호가 일치하지 않습니다.");
        }

        userRepository.delete(user);

        return new AuthResponse(true, "회원 탈퇴 완료");
    }

    // ===============================
    // 아이디 찾기
    // ===============================
    @Override
    public AuthResponse findUserId(FindIdRequest req) {

        Optional<Users> userOpt = userRepository.findByNameAndEmail(req.getName(), req.getEmail());
        if (!userOpt.isPresent()) {
            return new AuthResponse(false, "일치하는 회원 정보를 찾을 수 없습니다.");
        }

        return new AuthResponse(true, "아이디 찾기 성공", userOpt.get().getUserId());
    }

    // ===============================
    // 비밀번호 재설정 (1단계)
    // ===============================
    @Override
    public AuthResponse forgotPassword(ForgotPasswordRequest req) {

        passwordResetService.sendResetCode(req.getUserId(), req.getEmail());
        return new AuthResponse(true, "인증코드를 이메일로 전송했습니다.");
    }

    // ===============================
    // 비밀번호 재설정 (2단계)
    // ===============================
    @Override
    public AuthResponse resetPassword(PasswordResetConfirmRequest req) {

        passwordResetService.resetPassword(req.getEmail(), req.getCode(), req.getNewPassword());
        return new AuthResponse(true, "비밀번호가 변경되었습니다.");
    }

    // --------------------------------------------------------------------
    // 2. 이메일 인증: 여기서 비로소 Users 테이블에 INSERT
    // --------------------------------------------------------------------
    @Override
    public AuthResponse verifyEmail(String token) {

        Optional<EmailVerificationToken> opt = emailVerificationTokenRepository.findByToken(token);
        if (!opt.isPresent()) {
            return new AuthResponse(false, "유효하지 않거나 만료된 인증 링크입니다.", null);
        }

        EmailVerificationToken evt = opt.get();

        // 만료 체크
        if (evt.getExpiresAt().isBefore(LocalDateTime.now())) {
            emailVerificationTokenRepository.deleteById(evt.getId());
            return new AuthResponse(false, "인증 링크가 만료되었습니다. 다시 회원가입을 진행해 주세요.", null);
        }

        // 혹시 그 사이에 같은 아이디/이메일로 가입된 사람이 있으면 막기
        if (userRepository.findByUserId(evt.getUserId()).isPresent()) {
            emailVerificationTokenRepository.deleteById(evt.getId());
            return new AuthResponse(false, "이미 가입된 아이디입니다.", null);
        }
        if (userRepository.findByEmail(evt.getEmail()).isPresent()) {
            emailVerificationTokenRepository.deleteById(evt.getId());
            return new AuthResponse(false, "이미 가입된 이메일입니다.", null);
        }

        // 실제 Users INSERT (회사 정보 X)
        Users user = new Users();
        user.setUserId(evt.getUserId());
        user.setPassword(evt.getPassword()); // 이미 인코딩된 상태
        user.setName(evt.getName());
        user.setEmail(evt.getEmail());
        // user.setPhone(...) : 지금은 회원가입에서 안 받으니 null / 추후 Update에서 세팅
        user.setVerified(true); // 인증된 상태로만 Users에 들어옴

        userRepository.save(user);

        // 사용된 토큰 삭제
        emailVerificationTokenRepository.deleteById(evt.getId());

        return new AuthResponse(true, "이메일 인증이 완료되었습니다. 이제 로그인할 수 있습니다.", null);
    }

    // ===============================
    // 프로필 조회
    // ===============================
    @Override
    public AuthResponse getProfileByIdx(Long idx) {
        Optional<Users> userOpt = userRepository.findById(idx);
        if (!userOpt.isPresent()) {
            return new AuthResponse(false, "존재하지 않는 유저입니다.");
        }
        return new AuthResponse(true, "조회 성공", userOpt.get());
    }

    // ===============================
    // 프로필 수정
    // ===============================
    @Override
    public AuthResponse updateByIdx(Long idx, UpdateRequest req) {
        Optional<Users> userOpt = userRepository.findById(idx);
        if (!userOpt.isPresent()) {
            return new AuthResponse(false, "존재하지 않는 유저입니다.");
        }

        Users user = userOpt.get();

        // 기본 프로필 정보
        if (req.getName() != null) {
            user.setName(req.getName());
        }
        if (req.getPhone() != null) {
            user.setPhone(req.getPhone());
        }

        // ============================
        // 회사 관련 정보 업데이트
        // ============================
        String newCompanyName = req.getCardCompanyName();
        String newAddress = req.getAddress();

        boolean needCompanyMatch = false;

        // 1) 회사명 변경 여부 체크
        if (newCompanyName != null) {
            String trimmed = newCompanyName.trim();
            String current = user.getCardCompanyName();

            // 값이 바뀐 경우에만 매칭 다시 수행
            if (!trimmed.isEmpty() && (current == null || !trimmed.equals(current))) {
                user.setCardCompanyName(trimmed);
                needCompanyMatch = true;
            }
            // 빈 문자열로 들어온 경우 → 회사 정보 초기화
            if (trimmed.isEmpty()) {
                user.setCardCompanyName(null);
                user.setCompanyIdx(null);
            }
        }

        // 2) 주소가 새로 들어온 경우, 회사매칭 시 같이 사용
        if (newAddress != null && !newAddress.trim().isEmpty()) {
            // 주소는 Users에 굳이 저장 안 하고, 매칭용으로만 사용하고
            // 회사 공식 주소는 companies.address에 들어가게 설계하는게 깔끔
            // (원하면 Users 쪽에 companyAddress 필드 추가해서 같이 저장해도 됨)
            needCompanyMatch = true;
        }

        // 3) 부서 / 직책 / 팩스는 그대로 Users에 저장
        if (req.getDepartment() != null) {
            user.setDepartment(req.getDepartment());
        }
        if (req.getPosition() != null) {
            user.setPosition(req.getPosition());
        }
        if (req.getFax() != null) {
            user.setFax(req.getFax());
        }

        // 4) 회사명 변경(or 주소 입력) 시 회사매칭 서비스 호출
        if (needCompanyMatch && user.getCardCompanyName() != null && !user.getCardCompanyName().trim().isEmpty()) {

            String companyNameForMatch = user.getCardCompanyName();
            String addressForMatch = (newAddress != null ? newAddress : "");

            // 1차: 외부 API까지 사용하는 무거운 매칭
            Optional<Company> matched = companyInfoExtractService.extractAndSave(companyNameForMatch, addressForMatch);

            // 2차: 실패 시 DB 기반 가벼운 매칭/생성
            if (!matched.isPresent()) {
                matched = companyInfoExtractService.matchOrCreateCompany(companyNameForMatch, addressForMatch);
            }

            // 매칭 성공 시 Users.companyIdx 갱신
            matched.ifPresent(c -> user.setCompanyIdx(c.getIdx()));
        }

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return new AuthResponse(true, "정보 수정 완료", user);
    }

    // ===============================
    // 비밀번호 변경
    // ===============================
    @Override
    public AuthResponse changePasswordByIdx(Long idx, ChangePasswordRequest req) {

        if (req.getCurrentPassword() == null || req.getNewPassword() == null) {
            return new AuthResponse(false, "현재 비밀번호와 새 비밀번호가 모두 필요합니다.");
        }

        Optional<Users> userOpt = userRepository.findById(idx);
        if (!userOpt.isPresent()) {
            return new AuthResponse(false, "존재하지 않는 유저입니다.");
        }

        Users user = userOpt.get();

        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            return new AuthResponse(false, "현재 비밀번호가 일치하지 않습니다.");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        return new AuthResponse(true, "비밀번호 변경 성공");
    }

    // ===============================
    // Refresh Token 재발급
    // ===============================
    @Override
    public AuthResponse refresh(RefreshRequest req) {

        String refreshToken = req.getRefreshToken();

        if (refreshToken == null) {
            return new AuthResponse(false, "Refresh Token이 필요합니다.");
        }

        String userId = jwtUtil.getUserIdFromToken(refreshToken);

        if (userId == null || jwtUtil.isExpired(refreshToken)) {
            return new AuthResponse(false, "Refresh Token이 유효하지 않습니다.");
        }

        Optional<Users> userOpt = userRepository.findByUserId(userId);
        if (!userOpt.isPresent()) {
            return new AuthResponse(false, "존재하지 않는 유저입니다.");
        }

        Users user = userOpt.get();

        if (user.getRefreshToken() == null ||
                !user.getRefreshToken().equals(refreshToken)) {

            return new AuthResponse(false, "Refresh Token이 서버 정보와 일치하지 않습니다.");
        }

        // 새 토큰 발급
        String newAccess = jwtUtil.createAccessToken(userId);
        String newRefresh = jwtUtil.createRefreshToken(userId);

        user.setRefreshToken(newRefresh);
        userRepository.save(user);

        Map<String, String> map = new HashMap<>();
        map.put("accessToken", newAccess);
        map.put("refreshToken", newRefresh);

        return new AuthResponse(true, "토큰 재발급 성공", map);
    }

    // ===============================
    // ID / Email 중복 체크
    // ===============================
    @Override
    public boolean isUserIdDuplicate(String userId) {
        return userRepository.findByUserId(userId).isPresent();
    }

    @Override
    public boolean isEmailDuplicate(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}
