package com.altester.auth.service;

import com.altester.auth.dto.ChangePassDTO;
import com.altester.auth.exception.*;

public interface UserPassService {

    /**
     * Initiates the password reset process for a user.
     * Validates the user account, generates a reset code, and sends it
     * to the user's registered email address.
     *
     * @param email Email address of the user requesting password reset
     * @throws UserNotFoundException if user doesn't exist
     * @throws LdapUserOperationException if user is an admin or LDAP-created user
     * @throws CodeRequestTooSoonException if request is made too soon after previous code
     */
    void initiatePasswordReset(String email);

    /**
     * Completes the password reset process using the verification code.
     * Validates the provided code and updates the user's password if valid.
     *
     * @param changePassDTO Data transfer object containing email, verification code, and new password
     * @throws UserNotFoundException if user doesn't exist
     * @throws LdapUserOperationException if user is an admin or LDAP-created user
     * @throws PasswordResetCodeNotFoundException if no reset code exists
     * @throws PasswordResetCodeExpiredException if reset code has expired
     * @throws InvalidPasswordResetCodeException if reset code is incorrect
     */
    void resetPassword(ChangePassDTO changePassDTO);

    /**
     * Resends the password reset code to the user's email.
     * Generates a new reset code and sends it if enough time has passed
     * since the previous code was sent.
     *
     * @param email Email address of the user requesting a new reset code
     * @throws UserNotFoundException if user doesn't exist
     * @throws PasswordResetCodeNotFoundException if no existing code is found
     * @throws CodeRequestTooSoonException if request is made too soon after previous code
     */
    void resendResetCode(String email);
}