
package com.praveen.expensetracker.controller;

import com.praveen.expensetracker.dto.AuthDTO;
import com.praveen.expensetracker.dto.ProfileDTO;
import com.praveen.expensetracker.service.ProfileService;

import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping("/register")
    public ResponseEntity<?> registerProfile(
            @RequestBody ProfileDTO profileDTO) {

        try {
            ProfileDTO registeredProfile =
                    profileService.registerProfile(profileDTO);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(registeredProfile);

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("message", e.getReason()));
        }
    }

    @PostMapping("/resend-activation")
    public ResponseEntity<Map<String, String>> resendActivationEmail(
            @RequestParam String email) {

        try {
            profileService.resendActivationEmail(email);

            return ResponseEntity.ok(Map.of(
                    "message",
                    "Activation email sent successfully."
            ));

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("message", e.getReason()));
        }
    }

    @GetMapping("/activate")
    public ResponseEntity<String> activateProfile(
            @RequestParam String token) {

        boolean activated = profileService.activateProfile(token);

        if (activated) {
            return ResponseEntity.ok(
                    "Account activated successfully."
            );
        }

        return ResponseEntity.badRequest()
                .body("Invalid or expired activation token.");
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody AuthDTO authDTO) {

        try {
            if (!profileService.isAccountActive(authDTO.getEmail())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "message",
                                "Account is not active. Please activate it first."
                        ));
            }

            Map<String, Object> response =
                    profileService.authenticateAndGenerateToken(authDTO);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileDTO> getPublicProfile() {

        ProfileDTO profileDTO =
                profileService.getPublicProfile(null);

        return ResponseEntity.ok(profileDTO);
    }
}
