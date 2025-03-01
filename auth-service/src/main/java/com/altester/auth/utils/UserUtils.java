package com.altester.auth.utils;

import com.altester.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserUtils {

    private final UserRepository userRepository;
    private final Random random = new Random();

    public String generateUsername (String surname) {
        String prefix = surname.substring(0,3).toUpperCase();
        log.info("Generating username with prefix {}", prefix);

        String username;
        boolean isUnique;

        do {
            int randomNumber = 100 + random.nextInt(900);
            username = String.format("%sR%03d", prefix, randomNumber);
            isUnique = userRepository.findByUsername(username).isEmpty();
        } while (!isUnique);

        return username;
    }
}
