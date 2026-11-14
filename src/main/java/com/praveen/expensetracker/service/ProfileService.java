
package com.praveen.expensetracker.service;

import com.praveen.expensetracker.dto.AuthDTO;
import com.praveen.expensetracker.dto.ProfileDTO;
import com.praveen.expensetracker.entity.ProfileEntity;
import com.praveen.expensetracker.repository.ProfileRepository;
import com.praveen.expensetracker.util.JwtUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
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

        String email = profileDTO.getEmail().trim().toLowerCase(Locale.ROOT);

        if (profileRepository.findByEmail(email).isPresent()) {
            ProfileEntity existingProfile =
                    profileRepository.findByEmail(email).get();

            if (Boolean.TRUE.equals(existingProfile.getIsActive())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Email is already registered. Please log in."
                );
            }

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This email is already registered but not activated. Please resend the activation email."
            );
        }

        ProfileEntity newProfile = toEntity(profileDTO);
        newProfile.setEmail(email);
        newProfile.setActivationToken(UUID.randomUUID().toString());

        newProfile = profileRepository.save(newProfile);

        sendActivationEmail(newProfile);

        return toDTO(newProfile);
    }

    public void resendActivationEmail(String email) {

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        ProfileEntity profile = profileRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No account found with this email."
                ));

        if (Boolean.TRUE.equals(profile.getIsActive())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Account is already activated. Please log in."
            );
        }

        profile.setActivationToken(UUID.randomUUID().toString());

        profile = profileRepository.save(profile);

        sendActivationEmail(profile);
    }

    private void sendActivationEmail(ProfileEntity profile) {

        String activationLink = activationURL
                + "/api/v1.0/activate?token="
                + profile.getActivationToken();

        String subject = "Activate your Expense Tracker Account";

        String body = "Click the link to activate your account: "
                + activationLink;

        emailService.sendEmail(profile.getEmail(), subject, body);
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

        ProfileEntity profile = profileRepository
                .findByActivationToken(activationToken)
                .orElse(null);

        if (profile == null) {
            return false;
        }

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

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return profileRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found with email: " + email
                        ));
    }

    public ProfileDTO getPublicProfile(String email) {

        ProfileEntity profile = (email == null)
                ? getCurrentProfile()
                : profileRepository.findByEmail(email)
                  .orElseThrow(() ->
                               new UsernameNotFoundException(
                                       "User not found with email: " + email
                               ));

        return toDTO(profile);
    }

    public Map<String, Object> authenticateAndGenerateToken(AuthDTO authDTO) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authDTO.getEmail(),
                            authDTO.getPassword()
                    )
            );

            ProfileEntity profile = profileRepository
                    .findByEmail(authDTO.getEmail())
                    .orElseThrow(() ->
                            new UsernameNotFoundException(
                                    "User not found with email: " + authDTO.getEmail()
                            ));

            String token = jwtUtil.generateToken(profile.getEmail());

            ProfileDTO profileDTO = toDTO(profile);
            profileDTO.setPassword(null);

            return Map.of(
                    "token", token,
                    "user", profileDTO
            );

        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid email or password");
        }
    }
}
