package com.altester.auth.utils;

import com.altester.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserUtils {

    private final UserRepository userRepository;

    public String generateUsername (String surname) {
        String prefix = surname.substring(0,3).toUpperCase();
        log.info("Generating username with prefix {}", prefix);

        List<Integer> usedNumbers = userRepository.findUsedUsernameNumbers(prefix);

        int freeNumber = 1;
        for (int number : usedNumbers) {
            if (number != freeNumber) {
                break;
            }
            freeNumber++;
        }

        return String.format("%s%04d", prefix, freeNumber);
    }
}
