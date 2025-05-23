package com.altester.auth.service;

import com.altester.auth.dto.Auth.LoginResponse;
import com.altester.auth.dto.Auth.LoginUserDTO;
import com.altester.auth.exception.*;

public interface AuthService {

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
}
