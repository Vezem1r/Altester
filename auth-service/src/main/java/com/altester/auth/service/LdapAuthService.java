package com.altester.auth.service;

import com.altester.auth.dto.Auth.LoginResponse;
import com.altester.auth.dto.LdapLoginRequest;
import com.altester.auth.models.User;
import com.altester.auth.exception.LdapAuthException;

public interface LdapAuthService {

    /**
     * Authenticates a user against the LDAP directory.
     * Connects to the LDAP server, authenticates the user, and retrieves
     * user attributes to create or update the local user record.
     *
     * @param username LDAP username (cn attribute)
     * @param password User's LDAP password
     * @return User object containing user information retrieved from LDAP
     * @throws LdapAuthException if authentication fails or required attributes are missing
     */
    User authenticate(String username, String password);

    /**
     * Processes an LDAP login request and generates an authentication token.
     * Authenticates the user via LDAP and generates a JWT token for the session.
     *
     * @param request Data transfer object containing LDAP login credentials
     * @return LoginResponse containing the authentication token, user role, and status message
     * @throws LdapAuthException if LDAP authentication fails
     */
    LoginResponse login(LdapLoginRequest request);
}