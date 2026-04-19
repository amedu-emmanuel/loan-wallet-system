package com.koins.loanwallet.service.impl;

import com.koins.loanwallet.dto.request.ForgotPasswordRequest;
import com.koins.loanwallet.dto.request.LoginRequest;
import com.koins.loanwallet.dto.request.ResetPasswordRequest;
import com.koins.loanwallet.dto.request.SignupRequest;
import com.koins.loanwallet.dto.response.AuthResponse;
import com.koins.loanwallet.dto.response.OtpResponse;
import com.koins.loanwallet.dto.response.UserResponse;
import com.koins.loanwallet.entity.Otp;
import com.koins.loanwallet.entity.User;
import com.koins.loanwallet.entity.Wallet;
import com.koins.loanwallet.enums.AccountStatus;
import com.koins.loanwallet.enums.OtpPurpose;
import com.koins.loanwallet.enums.Role;
import com.koins.loanwallet.enums.WalletStatus;
import com.koins.loanwallet.exception.BadRequestException;
import com.koins.loanwallet.repository.OtpRepository;
import com.koins.loanwallet.repository.TokenBlacklistRepository;
import com.koins.loanwallet.repository.UserRepository;
import com.koins.loanwallet.repository.WalletRepository;
import com.koins.loanwallet.security.CustomUserPrincipal;
import com.koins.loanwallet.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private OtpRepository otpRepository;

    @Mock
    private TokenBlacklistRepository tokenBlacklistRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "otpExpiryMinutes", 10L);
    }

    @Test
    void signup_shouldCreateUserAndWalletSuccessfully() {
        SignupRequest request = new SignupRequest();
        request.setFullName("Emmanuel Amedu");
        request.setEmail("emmanuel@test.com");
        request.setPhoneNumber("08012345678");
        request.setPassword("password123");
        request.setBvnOrNin("12345678901");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(request.getPhoneNumber())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");

        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase())
                .phoneNumber(request.getPhoneNumber())
                .password("encoded-password")
                .bvnOrNin(request.getBvnOrNin())
                .accountStatus(AccountStatus.ACTIVE)
                .role(Role.USER)
                .build();
        savedUser.setCreatedAt(LocalDateTime.now());

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = authService.signup(request);

        assertNotNull(response);
        assertEquals("emmanuel@test.com", response.getEmail());
        assertEquals(Role.USER, response.getRole());
        assertEquals(AccountStatus.ACTIVE, response.getAccountStatus());

        verify(userRepository, times(1)).save(any(User.class));
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }

    @Test
    void signup_shouldThrowExceptionWhenEmailAlreadyExists() {
        SignupRequest request = new SignupRequest();
        request.setEmail("emmanuel@test.com");
        request.setPhoneNumber("08012345678");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.signup(request)
        );

        assertEquals("Email already exists", exception.getMessage());
        verify(walletRepository, never()).save(any());
    }

    @Test
    void login_shouldReturnTokenAndUserDetails() {
        LoginRequest request = new LoginRequest();
        request.setEmail("emmanuel@test.com");
        request.setPassword("password123");

        User user = User.builder()
                .id(UUID.randomUUID())
                .fullName("Emmanuel Amedu")
                .email("emmanuel@test.com")
                .phoneNumber("08012345678")
                .password("encoded-password")
                .bvnOrNin("12345678901")
                .accountStatus(AccountStatus.ACTIVE)
                .role(Role.USER)
                .build();
        user.setCreatedAt(LocalDateTime.now());

        when(userRepository.findByEmail("emmanuel@test.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(CustomUserPrincipal.class))).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("emmanuel@test.com", response.getUser().getEmail());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void forgotPassword_shouldGenerateOtpSuccessfully() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("emmanuel@test.com");

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("emmanuel@test.com")
                .role(Role.USER)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        when(userRepository.findByEmail("emmanuel@test.com")).thenReturn(Optional.of(user));
        when(otpRepository.findByUserAndPurposeAndUsedFalse(user, OtpPurpose.PASSWORD_RESET))
                .thenReturn(List.of());
        when(otpRepository.save(any(Otp.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OtpResponse response = authService.forgotPassword(request);

        assertNotNull(response);
        assertEquals("Password reset OTP generated successfully", response.getMessage());
        assertNotNull(response.getOtpCode());
        assertEquals(6, response.getOtpCode().length());

        verify(otpRepository).save(any(Otp.class));
    }

    @Test
    void resetPassword_shouldUpdatePasswordSuccessfully() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("emmanuel@test.com");
        request.setOtpCode("123456");
        request.setNewPassword("newpassword123");

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("emmanuel@test.com")
                .password("old-password")
                .build();

        Otp otp = Otp.builder()
                .user(user)
                .otpCode("123456")
                .purpose(OtpPurpose.PASSWORD_RESET)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();

        when(userRepository.findByEmail("emmanuel@test.com")).thenReturn(Optional.of(user));
        when(otpRepository.findTopByUserAndPurposeAndUsedFalseOrderByCreatedAtDesc(user, OtpPurpose.PASSWORD_RESET))
                .thenReturn(Optional.of(otp));
        when(passwordEncoder.encode("newpassword123")).thenReturn("encoded-new-password");

        authService.resetPassword(request);

        assertEquals("encoded-new-password", user.getPassword());
        assertTrue(otp.isUsed());

        verify(userRepository).save(user);
        verify(otpRepository).save(otp);
    }
}