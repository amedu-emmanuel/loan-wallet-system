package com.koins.loanwallet.controller;

import com.koins.loanwallet.dto.request.UpdateProfileRequest;
import com.koins.loanwallet.dto.response.ApiResponse;
import com.koins.loanwallet.dto.response.UserResponse;
import com.koins.loanwallet.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserResponse> getMyProfile() {
        return ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User profile retrieved successfully")
                .data(userService.getCurrentUserProfile())
                .build();
    }

    @PutMapping("/me")
    public ApiResponse<UserResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Profile updated successfully")
                .data(userService.updateProfile(request))
                .build();
    }
}