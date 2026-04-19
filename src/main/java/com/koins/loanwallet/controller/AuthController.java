package com.koins.loanwallet.controller;

import com.koins.loanwallet.dto.request.*;
import com.koins.loanwallet.dto.response.ApiResponse;
import com.koins.loanwallet.dto.response.AuthResponse;
import com.koins.loanwallet.dto.response.OtpResponse;
import com.koins.loanwallet.dto.response.UserResponse;
import com.koins.loanwallet.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ApiResponse<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User registered successfully")
                .data(authService.signup(request))
                .build();
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Login successful")
                .data(authService.login(request))
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        authService.logout(request);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Logout successful")
                .data(null)
                .build();
    }

    @PostMapping("/forgot-password")
    public ApiResponse<OtpResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ApiResponse.<OtpResponse>builder()
                .success(true)
                .message("OTP generated successfully")
                .data(authService.forgotPassword(request))
                .build();
    }

    @PostMapping("/resend-otp")
    public ApiResponse<OtpResponse> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        return ApiResponse.<OtpResponse>builder()
                .success(true)
                .message("OTP resent successfully")
                .data(authService.resendOtp(request))
                .build();
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Password reset successful")
                .data(null)
                .build();
    }
}