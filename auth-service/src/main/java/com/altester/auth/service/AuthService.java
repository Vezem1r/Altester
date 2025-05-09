package com.altester.auth.service;

import com.altester.auth.dto.Auth.LoginResponse;
import com.altester.auth.dto.Auth.LoginUserDTO;
import com.altester.auth.dto.Auth.RegisterUserDTO;
import com.altester.auth.dto.Auth.VerifyUserDTO;
import com.altester.auth.exception.*;

public interface AuthService {

  /**
   * Registers a new user in the system. Creates a new user account in disabled state and sends a
   * verification email with a confirmation code to complete registration.
   *
   * @param registerUserDTO Data transfer object containing registration information
   * @throws EmailAlreadyExistsException if the email is already in use by an enabled account
   * @throws CodeRequestTooSoonException if the user tries to register again too soon after a
   *     previous attempt
   */
  void register(RegisterUserDTO registerUserDTO);

  /**
   * Authenticates a user and generates an authentication token. Validates user credentials, updates
   * last login time, and generates a JWT token for authenticated session.
   *
   * @param loginUserDTO Data transfer object containing login credentials and preferences
   * @return LoginResponse containing the authentication token, user role, and status message
   * @throws UserNotFoundException if user doesn't exist
   * @throws UserDisabledException if user account is not verified
   * @throws InvalidCredentialsException if password doesn't match
   * @throws LdapUserOperationException if attempting to log in an LDAP-created user directly
   */
  LoginResponse signIn(LoginUserDTO loginUserDTO);

  /**
   * Verifies a user account using the provided verification code. Checks the code validity and
   * expiration, then enables the account if valid.
   *
   * @param verifyUserDto Data transfer object containing email and verification code
   * @throws UserNotFoundException if user doesn't exist
   * @throws VerificationCodeNotFoundException if verification code doesn't exist
   * @throws VerificationCodeExpiredException if verification code has expired
   * @throws InvalidVerificationCodeException if verification code is incorrect
   */
  void verifyUser(VerifyUserDTO verifyUserDto);

  /**
   * Resends a verification code to the user's email address. Generates a new verification code and
   * sends it to the user's email if the previous code is expired or if enough time has passed since
   * the last code was sent.
   *
   * @param email Email address of the user requesting a new verification code
   * @throws UserNotFoundException if user doesn't exist
   * @throws UserDisabledException if account is already verified
   * @throws VerificationCodeNotFoundException if verification code record doesn't exist
   * @throws CodeRequestTooSoonException if the request is made too soon after a previous code was
   *     sent
   */
  void resendVerificationCode(String email);
}
