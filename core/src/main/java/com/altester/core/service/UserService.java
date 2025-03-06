package com.altester.core.service;

import com.altester.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserDetails loadUserByUsernameOrEmail(String usernameOrEmail) {
        if (usernameOrEmail.contains("@")) {
            return userRepository.findByEmailWithGroups(usernameOrEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + usernameOrEmail));
        } else {
            return userRepository.findByUsernameWithGroups(usernameOrEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + usernameOrEmail));
        }
    }
}

