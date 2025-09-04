package soulfit.soulfit.authentication.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import soulfit.soulfit.authentication.dto.ChangeCredentialsRequest;
import soulfit.soulfit.authentication.dto.LoginRequest;
import soulfit.soulfit.authentication.dto.RegisterRequest;
import soulfit.soulfit.authentication.entity.AccountStatus;
import soulfit.soulfit.authentication.util.JwtUtil;
import soulfit.soulfit.authentication.dto.AuthResponse;
import soulfit.soulfit.authentication.entity.RefreshToken;
import soulfit.soulfit.authentication.entity.Role;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;

import soulfit.soulfit.profile.domain.Gender;
import soulfit.soulfit.profile.domain.UserProfile;
import soulfit.soulfit.profile.repository.UserProfileRepository;
import soulfit.soulfit.matching.profile.domain.MatchingProfile;
import soulfit.soulfit.matching.profile.domain.Religion;
import soulfit.soulfit.matching.profile.domain.DrinkingHabit;
import soulfit.soulfit.matching.profile.domain.SmokingHabit;
import soulfit.soulfit.matching.profile.domain.Visibility;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    private final Logger logger = LoggerFactory.getLogger(AuthService.class);

    public AuthResponse login(LoginRequest loginRequest) {
        UserAuth userAuth = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + loginRequest.getEmail()));

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userAuth.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtil.generateToken(authentication.getName());

        // Refresh Token 생성
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(authentication.getName());

        logger.info("authenticated as " + authentication.getName());

        return new AuthResponse(jwt, refreshToken.getToken(), userAuth.getUsername(), userAuth.getEmail(),userAuth.getId());
    }

    public String register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        UserAuth userAuth = new UserAuth(
                registerRequest.getUsername(),
                passwordEncoder.encode(registerRequest.getPassword()),
                registerRequest.getEmail()
        );
        userAuth.setRole(Role.USER);

        UserProfile userProfile = new UserProfile(
                userAuth,
                LocalDate.parse(registerRequest.getBirthDate(), DateTimeFormatter.ISO_LOCAL_DATE),
                Gender.valueOf(registerRequest.getGender().toUpperCase())
        );
        userAuth.setUserProfile(userProfile);


        // 3. MatchingProfile 생성
        MatchingProfile matchingProfile = MatchingProfile.builder()
                .userAuth(userAuth)
                .bio("")
                .job("")
                .heightCm(0)
                .weightKg(0)
                .religion(Religion.NONE)
                .smoking(SmokingHabit.NON_SMOKER)
                .drinking(DrinkingHabit.NEVER)
                .visibility(Visibility.PRIVATE)
                .idealTypes(new HashSet<>()) // 초기에는 이상형 키워드 없음
                .build();
        userAuth.setMatchingProfile(matchingProfile);

        userRepository.save(userAuth);

        return "User registered successfully!";
    }

    public AuthResponse refreshToken(String refreshTokenStr) {
        return refreshTokenService.findByToken(refreshTokenStr)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUsername)
                .map(username -> {
                    String newAccessToken = jwtUtil.generateToken(username);
                    String email = userRepository.findByUsername(username).get().getEmail();
                    Long id = userRepository.findByUsername(username).get().getId();
                    return new AuthResponse(newAccessToken, refreshTokenStr,username,email,id);
                })
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
    }

    public void logout(String accessToken, String username) {
        // 1. Access Token을 블랙리스트에 추가 (만료시간 정보도 함께 전달)
        if (accessToken != null) {
            try {
                Date expiration = jwtUtil.extractExpiration(accessToken);
                tokenBlacklistService.addToBlacklist(accessToken, username, expiration);
            } catch (Exception e) {
                // 토큰이 유효하지 않은 경우 무시
                logger.warn("Failed to extract expiration from token during logout: {}", e.getMessage());
            }
        }

        // 2. 해당 사용자의 모든 Refresh Token 삭제
        refreshTokenService.deleteAllByUsername(username);

        logger.info("User {} logged out successfully", username);
    }

    public void logoutFromAllDevices(String username) {
        // 모든 디바이스에서 로그아웃 (모든 refresh token 삭제)
        refreshTokenService.deleteAllByUsername(username);
        logger.info("User {} logged out from all devices", username);
    }

    public void changeCredentials(String currentUsername, ChangeCredentialsRequest request) {
        UserAuth userAuth = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String oldUsername = userAuth.getUsername();

        // 현재 비밀번호 검증
        if (!passwordEncoder.matches(request.getCurrentPassword(), userAuth.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // 비밀번호 변경
        if (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) {
            userAuth.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        userRepository.save(userAuth);

        logout(request.getAccessToken(),oldUsername);

        logger.info("User '{}' updated credentials successfully", currentUsername);
    }

    // soft deletion
    @Transactional
    public void deactivateCurrentUser() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        UserAuth user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + currentUsername));

        user.setEnabled(false);  // 비활성화
        user.setAccountStatus(AccountStatus.WITHDRAWN);  // 상태를 '삭제됨'으로 변경

        userRepository.save(user);
    }

}