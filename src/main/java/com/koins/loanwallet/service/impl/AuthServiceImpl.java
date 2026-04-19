package com.koins.loanwallet.service.impl;

import com.koins.loanwallet.dto.request.*;
import com.koins.loanwallet.dto.response.AuthResponse;
import com.koins.loanwallet.dto.response.OtpResponse;
import com.koins.loanwallet.dto.response.UserResponse;
import com.koins.loanwallet.entity.Otp;
import com.koins.loanwallet.entity.TokenBlacklist;
import com.koins.loanwallet.entity.User;
import com.koins.loanwallet.entity.Wallet;
import com.koins.loanwallet.enums.*;
import com.koins.loanwallet.exception.BadRequestException;
import com.koins.loanwallet.exception.ResourceNotFoundException;
import com.koins.loanwallet.repository.OtpRepository;
import com.koins.loanwallet.repository.TokenBlacklistRepository;
import com.koins.loanwallet.repository.UserRepository;
import com.koins.loanwallet.repository.WalletRepository;
import com.koins.loanwallet.security.CustomUserPrincipal;
import com.koins.loanwallet.security.JwtService;
import com.koins.loanwallet.service.AuthService;
import com.koins.loanwallet.util.AppConstants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final OtpRepository otpRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("${app.otp.expiry-minutes}")
    private long otpExpiryMinutes;

    @Override
    @Transactional
    public UserResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BadRequestException("Phone number already exists");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .bvnOrNin(request.getBvnOrNin())
                .accountStatus(AccountStatus.ACTIVE)
                .role(Role.USER)
                .build();

        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        Wallet wallet = Wallet.builder()
                .user(savedUser)
                .balance(AppConstants.DEFAULT_WALLET_BALANCE)
                .currency(AppConstants.DEFAULT_CURRENCY)
                .status(WalletStatus.ACTIVE)
                .build();

        wallet.setCreatedAt(LocalDateTime.now());

        walletRepository.save(wallet);

        return mapToUserResponse(savedUser);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = jwtService.generateToken(new CustomUserPrincipal(user));

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(mapToUserResponse(user))
                .build();
    }

    @Override
    public void logout(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BadRequestException("Authorization token is missing");
        }

        String token = authHeader.substring(7);

        TokenBlacklist blacklist = TokenBlacklist.builder()
                .token(token)
                .expiresAt(jwtService.extractExpiration(token).toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDateTime())
                .blacklistedAt(LocalDateTime.now())
                .build();

        tokenBlacklistRepository.save(blacklist);
    }

    @Override
    @Transactional
    public OtpResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        otpRepository.findByUserAndPurposeAndUsedFalse(user, OtpPurpose.PASSWORD_RESET)
                .forEach(existingOtp -> {
                    existingOtp.setUsed(true);
                    otpRepository.save(existingOtp);
                });

        String otpCode = generateOtp();

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(otpExpiryMinutes);

        Otp otp = Otp.builder()
                .user(user)
                .otpCode(otpCode)
                .purpose(OtpPurpose.PASSWORD_RESET)
                .expiresAt(expiresAt)
                .used(false)
                .build();

        otp.setCreatedAt(LocalDateTime.now());

        otpRepository.save(otp);

        return OtpResponse.builder()
                .message("Password reset OTP generated successfully")
                .otpCode(otpCode)
                .expiresAt(expiresAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }

    @Override
    @Transactional
    public OtpResponse resendOtp(ResendOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        otpRepository.findByUserAndPurposeAndUsedFalse(user, OtpPurpose.PASSWORD_RESET)
                .forEach(existingOtp -> {
                    existingOtp.setUsed(true);
                    otpRepository.save(existingOtp);
                });

        String otpCode = generateOtp();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(otpExpiryMinutes);

        Otp otp = Otp.builder()
                .user(user)
                .otpCode(otpCode)
                .purpose(OtpPurpose.PASSWORD_RESET)
                .expiresAt(expiresAt)
                .used(false)
                .build();

        otp.setCreatedAt(LocalDateTime.now());

        otpRepository.save(otp);

        return OtpResponse.builder()
                .message("OTP resent successfully")
                .otpCode(otpCode)
                .expiresAt(expiresAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Otp otp = otpRepository.findTopByUserAndPurposeAndUsedFalseOrderByCreatedAtDesc(user, OtpPurpose.PASSWORD_RESET)
                .orElseThrow(() -> new BadRequestException("No valid OTP found"));

        if (!otp.getOtpCode().equals(request.getOtpCode())) {
            throw new BadRequestException("Invalid OTP");
        }

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP has expired");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        otp.setUsed(true);
        otpRepository.save(otp);
    }

    private String generateOtp() {
        int otp = 100000 + new Random().nextInt(900000);
        return String.valueOf(otp);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .bvnOrNin(user.getBvnOrNin())
                .accountStatus(user.getAccountStatus())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}