package soulfit.soulfit.authentication.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import soulfit.soulfit.authentication.dto.ChangeCredentialsRequest;
import soulfit.soulfit.authentication.dto.LoginRequest;
import soulfit.soulfit.authentication.dto.RegisterRequest;
import soulfit.soulfit.authentication.util.JwtUtil;
import soulfit.soulfit.authentication.dto.AuthResponse;
import soulfit.soulfit.authentication.entity.RefreshToken;
import soulfit.soulfit.authentication.entity.Role;
import soulfit.soulfit.authentication.entity.User;
import soulfit.soulfit.authentication.repository.UserRepository;

import java.util.Date;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

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
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtil.generateToken(authentication.getName());

        // Refresh Token 생성
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(authentication.getName());

        logger.info("authenticated as " + authentication.getName());

        return new AuthResponse(jwt, refreshToken.getToken(), loginRequest.getUsername());
    }

    public String register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        User user = new User(
                registerRequest.getUsername(),
                passwordEncoder.encode(registerRequest.getPassword()),
                registerRequest.getEmail()
        );
        user.setRole(Role.USER);
        userRepository.save(user);

        return "User registered successfully!";
    }

    public AuthResponse refreshToken(String refreshTokenStr) {
        return refreshTokenService.findByToken(refreshTokenStr)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUsername)
                .map(username -> {
                    String newAccessToken = jwtUtil.generateToken(username);
                    return new AuthResponse(newAccessToken, refreshTokenStr, username);
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
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String oldUsername = user.getUsername();

        // 현재 비밀번호 검증
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // 사용자명 변경
        if (request.getNewUsername() != null && !request.getNewUsername().equals(currentUsername)) {
            if (userRepository.existsByUsername(request.getNewUsername())) {
                throw new RuntimeException("New username is already taken");
            }
            user.setUsername(request.getNewUsername());
        }

        // 비밀번호 변경
        if (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        userRepository.save(user);

        logout(request.getAccessToken(),oldUsername);

        logger.info("User '{}' updated credentials successfully", currentUsername);
    }

}