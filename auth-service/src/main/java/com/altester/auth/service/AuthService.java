package com.altester.auth.service;

import com.altester.auth.dto.Auth.LoginUserDTO;
import com.altester.auth.dto.Auth.RegisterUserDTO;
import com.altester.auth.dto.Auth.VerifyUserDTO;
import com.altester.auth.exception.BadRequest;
import com.altester.auth.models.Codes;
import com.altester.auth.models.User;
import com.altester.auth.models.enums.CodeType;
import com.altester.auth.models.enums.EmailType;
import com.altester.auth.models.enums.RolesEnum;
import com.altester.auth.repository.CodeRepository;
import com.altester.auth.repository.UserRepository;
import com.altester.auth.utils.EmailUtils;
import com.altester.auth.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserUtils userUtils;
    private final CodeRepository codeRepository;
    private final EmailUtils emailUtils;

    public User signUp(RegisterUserDTO registerUserDTO){
        log.info("Attempting to register user with email: {}", registerUserDTO.getEmail());

        Optional<User> existingUserByEmail = userRepository.findByEmail(registerUserDTO.getEmail());

        if (existingUserByEmail.isPresent()) {
            User userByEmail = existingUserByEmail.get();
            if (userByEmail.isEnabled()) {
                log.error("User with email '{}' already exists.", registerUserDTO.getEmail());
                throw new BadRequest("User with this email already exists.");
            } else {
                List<Codes> codes = codeRepository.findAllByUser(userByEmail);
                codeRepository.deleteAll(codes);
                userRepository.delete(userByEmail);
                log.info("Deleted disabled user with email: {}", registerUserDTO.getEmail());
            }
        }

        User user = User.builder()
                .name(registerUserDTO.getName())
                .surname(registerUserDTO.getSurname())
                .email(registerUserDTO.getEmail())
                .password(passwordEncoder.encode(registerUserDTO.getPassword()))
                .created(LocalDateTime.now())
                .lastLogin(LocalDateTime.now())
                .enabled(false)
                .role(RolesEnum.STUDENT)
                .isRegistered(true)
                .username(userUtils.generateUsername(registerUserDTO.getSurname()))
                .build();

        Codes code = Codes.builder()
                .code(generateVerificationCode())
                .expiration(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .codeType(CodeType.VERIFICATION)
                .sendAt(LocalDateTime.now())
                .build();


        userRepository.save(user);
        codeRepository.save(code);

        emailUtils.sendVerificationEmail(user, EmailType.REGISTER);
        log.info("Registered user with email: {}", registerUserDTO.getEmail());
        return user;
    }

    public User signIn(LoginUserDTO loginUserDTO) {
        String usernameOrEmail = loginUserDTO.getUsernameOrEmail();
        Optional<User> optionalUser;

        if (usernameOrEmail.contains("@")) {
            log.info("Attempting to login user with email: {}", usernameOrEmail);
            optionalUser = userRepository.findByEmail(usernameOrEmail);
        } else {
            log.info("Attempting to login user with username: {}", usernameOrEmail);
            optionalUser = userRepository.findByUsername(usernameOrEmail.toUpperCase());
        }

        if (optionalUser.isEmpty()) {
            log.error("User with email '{}' not found.", usernameOrEmail);
            throw new UsernameNotFoundException("User with email " + usernameOrEmail + " not found.");
        }

        User user = optionalUser.get();

        if (!user.isEnabled()) {
            log.warn("User with email '{}' is disabled.", usernameOrEmail);
            throw new RuntimeException("User with email " + usernameOrEmail + " is disabled.");
        }

        if (!passwordEncoder.matches(loginUserDTO.getPassword(), user.getPassword())) {
            log.error("Invalid password for user '{}'", usernameOrEmail);
            throw new RuntimeException("Invalid credentials");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        log.info("Logged in user with email: {}", usernameOrEmail);
        return user;
    }

    public void verifyUser(VerifyUserDTO verifyUserDto) {
        Optional<User> optionalUser = userRepository.findByEmail(verifyUserDto.getEmail());
        if (optionalUser.isEmpty()) {
            log.error("User not found for verification: {}", verifyUserDto.getEmail());
            throw new RuntimeException("User not found");
        }

        Optional<Codes> optionalCode = codeRepository.findByUserAndCodeType(optionalUser.get(), CodeType.VERIFICATION);
        if (optionalCode.isEmpty()) {
            log.error("Code not found for user: {}", verifyUserDto.getEmail());
            throw new RuntimeException("Code not found");
        }

        User user = optionalUser.get();
        Codes code = optionalCode.get();

        if (code.getExpiration().isBefore(LocalDateTime.now())) {
            log.error("Verification code has expired for user: {}", verifyUserDto.getEmail());
            throw new RuntimeException("Verification code has expired");
        }
        if (code.getCode().equals(verifyUserDto.getVerificationCode())) {
            user.setEnabled(true);
            codeRepository.delete(code);
            userRepository.save(user);
            log.info("User verified successfully: {}", verifyUserDto.getEmail());
        } else {
            log.error("Invalid verification code for user: {}", verifyUserDto.getEmail());
            throw new RuntimeException("Invalid verification code");
        }
    }

    public void resendVerificationCode(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            Optional<Codes> optionalCode = codeRepository.findByUserAndCodeType(optionalUser.get(), CodeType.VERIFICATION);
            if (user.isEnabled()) {
                log.warn("Account is already verified for user: {}", email);
                throw new RuntimeException("Account is already verified");
            }

            if (optionalCode.isEmpty()) {
                log.error("Resend code not found for user: {}", email);
                throw new RuntimeException("Code not found");
            }

            Codes code = optionalCode.get();

            if (code.getSendAt().plusSeconds(59).isAfter(LocalDateTime.now())) {
                log.warn("Verification code was sent less than a minute ago for user: {}", email);
                throw new RuntimeException("Verification code was sent less than a minute ago");
            }

            code.setCode(generateVerificationCode());
            code.setExpiration(LocalDateTime.now().plusMinutes(15));
            emailUtils.sendVerificationEmail(user, EmailType.REGISTER);
            code.setSendAt(LocalDateTime.now());
            codeRepository.save(code);
            log.info("Resent verification code to user: {}", email);
        } else {
            log.error("User not found for resending verification code: {}", email);
            throw new RuntimeException("User not found");
        }
    }

    public String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}
