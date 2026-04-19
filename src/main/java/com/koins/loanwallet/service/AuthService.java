package com.koins.loanwallet.service;

import com.koins.loanwallet.dto.request.*;
import com.koins.loanwallet.dto.response.AuthResponse;
import com.koins.loanwallet.dto.response.OtpResponse;
import com.koins.loanwallet.dto.response.UserResponse;

import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    UserResponse signup(SignupRequest request);

    AuthResponse login(LoginRequest request);

    void logout(HttpServletRequest request);

    OtpResponse forgotPassword(ForgotPasswordRequest request);

    OtpResponse resendOtp(ResendOtpRequest request);

    void resetPassword(ResetPasswordRequest request);
}