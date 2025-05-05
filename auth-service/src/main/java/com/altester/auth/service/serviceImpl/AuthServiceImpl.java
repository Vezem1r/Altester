package com.altester.auth.service.serviceImpl;

import com.altester.auth.dto.Auth.LoginResponse;
import com.altester.auth.dto.Auth.LoginUserDTO;
import com.altester.auth.dto.Auth.RegisterUserDTO;
import com.altester.auth.dto.Auth.VerifyUserDTO;
import com.altester.auth.exception.*;
import com.altester.auth.models.Codes;
import com.altester.auth.models.User;
import com.altester.auth.models.enums.CodeType;
import com.altester.auth.models.enums.EmailType;
import com.altester.auth.models.enums.RolesEnum;
import com.altester.auth.repository.CodeRepository;
import com.altester.auth.repository.UserRepository;
import com.altester.auth.service.AuthService;
import com.altester.auth.utils.EmailUtils;
import com.altester.auth.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserUtils userUtils;
    private final CodeRepository codeRepository;
    private final EmailUtils emailUtils;
    private final JwtService jwtService;

    @Override
    public void register(RegisterUserDTO registerUserDTO){
        log.info("Attempting to register user with email: {}", registerUserDTO.getEmail());

        handleExistingUser(registerUserDTO.getEmail());

        User user = createUserFromDTO(registerUserDTO);
        userRepository.save(user);

        createAndSendVerificationCode(user);

        log.info("Registered user with email: {}", registerUserDTO.getEmail());
    }

    @Override
    public LoginResponse signIn(LoginUserDTO loginUserDTO) {
        String usernameOrEmail = loginUserDTO.getUsernameOrEmail();
        User user = findUserByLoginCredentials(usernameOrEmail);

        validateUserForLogin(user, usernameOrEmail, loginUserDTO.getPassword());

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtService.generateToken(user, user.getRole().name(), loginUserDTO.isRememberMe());

        return new LoginResponse(token, user.getRole().toString(), "Login successful");
    }

    @Override
    public void verifyUser(VerifyUserDTO verifyUserDto) {
        User user = findUserByEmailOrThrow(verifyUserDto.getEmail());
        Codes code = findCodeByUserAndTypeOrThrow(user);

        validateVerificationCode(user, code, verifyUserDto.getVerificationCode());
    }

    @Override
    public void resendVerificationCode(String email) {
        User user = findUserByEmailOrThrow(email);

        validateUserForResendCode(user);

        Codes code = findCodeByUserAndTypeOrThrow(user);

        validateCodeResendTime(code, email);

        regenerateAndSendVerificationCode(user, code);
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }

    private User findUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new UserNotFoundException(email);
                });
    }

    private Codes findCodeByUserAndTypeOrThrow(User user) {
        return codeRepository.findByUserAndCodeType(user, CodeType.VERIFICATION)
                .orElseThrow(() -> {
                    log.error("Code not found for user: {}", user.getEmail());
                    return new VerificationCodeNotFoundException(user.getEmail());
                });
    }

    private void createAndSendVerificationCode(User user) {
        Codes code = Codes.builder()
                .code(generateVerificationCode())
                .expiration(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .codeType(CodeType.VERIFICATION)
                .sendAt(LocalDateTime.now())
                .build();

        codeRepository.save(code);
        emailUtils.sendVerificationEmail(user, EmailType.REGISTER);
    }

    private void handleExistingUser(String email) {
        Optional<User> existingUserByEmail = userRepository.findByEmail(email);

        if (existingUserByEmail.isPresent()) {
            User userByEmail = existingUserByEmail.get();
            if (userByEmail.isEnabled()) {
                log.error("User with email '{}' already exists.", email);
                throw new EmailAlreadyExistsException(email);
            } else {
                handleDisabledExistingUser(userByEmail);
            }
        }
    }

    private void handleDisabledExistingUser(User user) {
        Optional<Codes> optionalCode = codeRepository.findByUserAndCodeType(user, CodeType.VERIFICATION);

        if (optionalCode.isPresent()) {
            Codes code = optionalCode.get();
            if (code.getSendAt().plusMinutes(5).isAfter(LocalDateTime.now())) {
                log.warn("User has been created less than 5 minutes ago: {}", user.getEmail());
                throw new CodeRequestTooSoonException("Verification", user.getEmail());
            }
        }

        List<Codes> codes = codeRepository.findAllByUser(user);
        codeRepository.deleteAll(codes);
        userRepository.delete(user);
        log.info("Deleted disabled user with email: {}", user.getEmail());
    }

    private User createUserFromDTO(RegisterUserDTO dto) {
        return User.builder()
                .name(dto.getName())
                .surname(dto.getSurname())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .created(LocalDateTime.now())
                .lastLogin(LocalDateTime.now())
                .enabled(false)
                .role(RolesEnum.STUDENT)
                .isRegistered(true)
                .username(userUtils.generateUsername(dto.getSurname()))
                .build();
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

    private void validateVerificationCode(User user, Codes code, String verificationCode) {
        if (code.getExpiration().isBefore(LocalDateTime.now())) {
            log.error("Verification code has expired for user: {}", user.getEmail());
            throw new VerificationCodeExpiredException(user.getEmail());
        }

        if (code.getCode().equals(verificationCode)) {
            user.setEnabled(true);
            codeRepository.delete(code);
            userRepository.save(user);
            log.info("User verified successfully: {}", user.getEmail());
        } else {
            log.error("Invalid verification code for user: {}", user.getEmail());
            throw new InvalidVerificationCodeException(user.getEmail());
        }
    }

    private void validateUserForResendCode(User user) {
        if (user.isEnabled()) {
            log.warn("Account is already verified for user: {}", user.getEmail());
            throw new UserDisabledException(user.getEmail());
        }
    }

    private void validateCodeResendTime(Codes code, String email) {
        if (code.getSendAt().plusSeconds(59).isAfter(LocalDateTime.now())) {
            log.warn("Verification code was sent less than a minute ago for user: {}", email);
            throw new CodeRequestTooSoonException("Verification", email);
        }
    }

    private void regenerateAndSendVerificationCode(User user, Codes code) {
        code.setCode(generateVerificationCode());
        code.setExpiration(LocalDateTime.now().plusMinutes(15));
        code.setSendAt(LocalDateTime.now());
        codeRepository.save(code);

        emailUtils.sendVerificationEmail(user, EmailType.REGISTER);
        log.info("Resent verification code to user: {}", user.getEmail());
    }
}