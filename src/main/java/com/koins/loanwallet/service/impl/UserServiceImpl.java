package com.koins.loanwallet.service.impl;

import com.koins.loanwallet.dto.request.UpdateProfileRequest;
import com.koins.loanwallet.dto.response.UserResponse;
import com.koins.loanwallet.entity.User;
import com.koins.loanwallet.exception.BadRequestException;
import com.koins.loanwallet.exception.ResourceNotFoundException;
import com.koins.loanwallet.repository.UserRepository;
import com.koins.loanwallet.security.SecurityUtils;
import com.koins.loanwallet.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponse getCurrentUserProfile() {
        UUID userId = SecurityUtils.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return mapToUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        userRepository.findByPhoneNumber(request.getPhoneNumber())
                .ifPresent(existingUser -> {
                    if (!existingUser.getId().equals(userId)) {
                        throw new BadRequestException("Phone number already exists");
                    }
                });

        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setBvnOrNin(request.getBvnOrNin());

        User updatedUser = userRepository.save(user);

        return mapToUserResponse(updatedUser);
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