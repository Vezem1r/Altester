package com.altester.auth.service.serviceImpl;

import com.altester.auth.dto.Auth.LoginResponse;
import com.altester.auth.dto.Auth.LoginUserDTO;
import com.altester.auth.exception.*;
import com.altester.auth.models.User;
import com.altester.auth.repository.UserRepository;
import com.altester.auth.service.AuthService;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  @Override
  public LoginResponse signIn(LoginUserDTO loginUserDTO) {
    String usernameOrEmail = loginUserDTO.getUsernameOrEmail();
    User user = findUserByLoginCredentials(usernameOrEmail);

    validateUserForLogin(user, usernameOrEmail, loginUserDTO.getPassword());

    user.setLastLogin(LocalDateTime.now());
    userRepository.save(user);

    String token =
        jwtService.generateToken(user, user.getRole().name(), loginUserDTO.isRememberMe());

    return new LoginResponse(token, user.getRole().toString(), "Login successful");
  }

  private User findUserByLoginCredentials(String usernameOrEmail) {
    Optional<User> optionalUser;

    if (usernameOrEmail.contains("@")) {
      log.info("Attempting to login user with email: {}", usernameOrEmail);
      optionalUser = userRepository.findByEmail(usernameOrEmail);
    } else {
      log.info("Attempting to login user with username: {}", usernameOrEmail);
      optionalUser = userRepository.findByUsername(usernameOrEmail.toUpperCase());
    }

    if (optionalUser.isEmpty()) {
      log.error("User with identifier '{}' not found.", usernameOrEmail);
      throw new UserNotFoundException(usernameOrEmail);
    }

    return optionalUser.get();
  }

  private void validateUserForLogin(User user, String usernameOrEmail, String password) {
    if (!user.isRegistered()) {
      log.error("User was created via LDAP {}", user.getEmail());
      throw new LdapUserOperationException(user.getUsername(), "direct login");
    }

    if (!user.isEnabled()) {
      log.warn("User with email '{}' is disabled.", usernameOrEmail);
      throw new UserDisabledException(usernameOrEmail);
    }

    if (!passwordEncoder.matches(password, user.getPassword())) {
      log.error("Invalid password for user '{}'", usernameOrEmail);
      throw new InvalidCredentialsException();
    }

    log.info("Logged in user with email: {}", usernameOrEmail);
  }
}
