package com.praveen.expensetracker.service;

import java.util.Collections;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.praveen.expensetracker.repository.ProfileRepository;
import com.praveen.expensetracker.entity.ProfileEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppUserDetailService implements UserDetailsService {
    
    private final ProfileRepository profileRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        ProfileEntity profile = profileRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return User.builder()
            .username(profile.getEmail())
            .password(profile.getPassword()) 
            .authorities(Collections.emptyList()) // <-- fixed: added ()
            .build();
    }
}
