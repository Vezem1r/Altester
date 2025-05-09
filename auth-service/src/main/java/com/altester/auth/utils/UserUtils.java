package com.altester.auth.utils;

import com.altester.auth.repository.UserRepository;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserUtils {

  private final UserRepository userRepository;
  private final Random random;

  private static final int MIN_BOUNDARY = 900000;
  private static final int MAX_BOUNDARY = 100000;

  public String generateUsername(String surname) {
    String prefix = surname.substring(0, 3).toUpperCase();
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

  public String generateVerificationCode() {
    return String.valueOf(random.nextInt(MIN_BOUNDARY) + MAX_BOUNDARY);
  }
}
