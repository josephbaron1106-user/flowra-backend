package com.flowra.flowra_backend.service;

import com.flowra.flowra_backend.dto.UserDTO;
import com.flowra.flowra_backend.entity.User;
import com.flowra.flowra_backend.exception.BadRequestException;
import com.flowra.flowra_backend.exception.ResourceNotFoundException;
import com.flowra.flowra_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserDTO.Response createUser(UserDTO.Request request) {
        String cleanEmail = request.getEmail().toLowerCase().trim();
        if (userRepository.existsByEmail(cleanEmail)) {
            throw new BadRequestException("An account already exists with email: " + cleanEmail);
        }

        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(cleanEmail)
                .passwordHash(request.getPassword()) // When security added, hash password
                .phone(request.getPhone())
                .role(request.getRole() != null ? request.getRole() : "customer")
                .avatarUrl(request.getAvatarUrl())
                .isActive(true)
                .build();

        User saved = userRepository.save(user);
        return mapToResponse(saved);
    }

    public UserDTO.Response login(UserDTO.LoginRequest request) {
        String cleanEmail = request.getEmail().toLowerCase().trim();
        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new ResourceNotFoundException("No account found with this email: " + cleanEmail + ". Please create an account first."));

        if (!user.getPasswordHash().equals(request.getPassword())) {
            throw new BadRequestException("Incorrect password. Please try again.");
        }

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new BadRequestException("This account has been deactivated. Please contact support.");
        }

        return mapToResponse(user);
    }

    public List<UserDTO.Response> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public UserDTO.Response getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToResponse(user);
    }

    @Transactional
    public UserDTO.Response updateUser(Long id, UserDTO.Request request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());
        if (request.getRole() != null) user.setRole(request.getRole());

        User updated = userRepository.save(user);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    public UserDTO.Response mapToResponse(User user) {
        return UserDTO.Response.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .avatarUrl(user.getAvatarUrl())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
