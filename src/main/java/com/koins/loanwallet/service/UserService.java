package com.koins.loanwallet.service;

import com.koins.loanwallet.dto.request.UpdateProfileRequest;
import com.koins.loanwallet.dto.response.UserResponse;

public interface UserService {

    UserResponse getCurrentUserProfile();

    UserResponse updateProfile(UpdateProfileRequest request);
}