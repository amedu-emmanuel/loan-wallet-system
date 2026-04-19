package com.koins.loanwallet.controller;

import com.koins.loanwallet.dto.request.UpdateProfileRequest;
import com.koins.loanwallet.dto.response.ApiResponse;
import com.koins.loanwallet.dto.response.UserResponse;
import com.koins.loanwallet.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Users", description = "User profile management APIs")

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get current authenticated user profile")
    @GetMapping("/me")
    public ApiResponse<UserResponse> getMyProfile() {
        return ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User profile retrieved successfully")
                .data(userService.getCurrentUserProfile())
                .build();
    }

    @Operation(summary = "Update current authenticated user profile")
    @PutMapping("/me")
    public ApiResponse<UserResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Profile updated successfully")
                .data(userService.updateProfile(request))
                .build();
    }
}