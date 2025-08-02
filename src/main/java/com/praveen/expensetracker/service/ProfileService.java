package com.praveen.expensetracker.service;

import com.praveen.expensetracker.dto.AuthDTO;
import com.praveen.expensetracker.dto.ProfileDTO;
import com.praveen.expensetracker.entity.ProfileEntity;
import com.praveen.expensetracker.repository.ProfileRepository;
import com.praveen.expensetracker.util.JwtUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final AuthenticationManager authenticationManager;
    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${app.activation.url}")
    private String activationURL;

    public ProfileDTO registerProfile(ProfileDTO profileDTO) {
        ProfileEntity newProfile = toEntity(profileDTO);
        newProfile.setActivationToken(UUID.randomUUID().toString());
        newProfile = profileRepository.save(newProfile);

        String activationLink = activationURL+"/api/v1.0/activate?token=" + newProfile.getActivationToken();
        String subject = "Activate your Expense Tracker Account";
        String body = "Click the link to activate your account: " + activationLink;
        emailService.sendEmail(newProfile.getEmail(), subject, body);

        return toDTO(newProfile);
    }

    public ProfileEntity toEntity(ProfileDTO profileDTO) {
        return ProfileEntity.builder()
            .id(profileDTO.getId())
            .fullName(profileDTO.getFullName())
            .email(profileDTO.getEmail())
            .password(passwordEncoder.encode(profileDTO.getPassword()))
            .profileImage(profileDTO.getProfileImage())
            .createdAt(profileDTO.getCreatedAt())
            .updatedAt(profileDTO.getUpdatedAt())
            .build();
    }

    public ProfileDTO toDTO(ProfileEntity profileEntity) {
        return ProfileDTO.builder()
            .id(profileEntity.getId())
            .fullName(profileEntity.getFullName())
            .email(profileEntity.getEmail())
            .profileImage(profileEntity.getProfileImage())
            .createdAt(profileEntity.getCreatedAt())
            .updatedAt(profileEntity.getUpdatedAt())
            .build();
    }

    public boolean activateProfile(String activationToken) {
        ProfileEntity profile = profileRepository.findByActivationToken(activationToken).orElse(null);
        if (profile == null) return false;

        profile.setIsActive(true);
        profile.setActivationToken(null);
        profileRepository.save(profile);
        return true;
    }

    public boolean isAccountActive(String email) {
        return profileRepository.findByEmail(email)
            .map(ProfileEntity::getIsActive)
            .orElse(false);
    }

    public ProfileEntity getCurrentProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return profileRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    public ProfileDTO getPublicProfile(String email) {
        ProfileEntity profile = (email == null)
            ? getCurrentProfile()
            : profileRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return toDTO(profile);
    }

    public Map<String, Object> authenticateAndGenerateToken(AuthDTO authDTO) {
    try {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(authDTO.getEmail(), authDTO.getPassword())
        );

        ProfileEntity profile = profileRepository.findByEmail(authDTO.getEmail())
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + authDTO.getEmail()));

        String token = jwtUtil.generateToken(profile.getEmail());

        ProfileDTO profileDTO = toDTO(profile);
        profileDTO.setPassword(null); // to avoid exposing password

        return Map.of(
            "token", token,
            "user", profileDTO
        );

    } catch (AuthenticationException e) {
        throw new RuntimeException("Invalid email or password");
    }
}

}
