package com.altester.auth.service;

import com.altester.auth.dto.EmailConfirmDTO;
import com.altester.auth.dto.EmailInitDTO;
import com.altester.auth.dto.EmailResendDTO;
import com.altester.auth.exception.*;

public interface UserEmailService {

    /**
     * Initiates the email change process for a user.
     * Validates user credentials, generates a verification code,
     * and sends it to the new email address.
     *
     * @param emailInitDTO Data transfer object containing username, password, and new email
     * @throws UserNotFoundException if user doesn't exist
     * @throws LdapUserOperationException if user is an admin or LDAP-created user
     * @throws InvalidCredentialsException if password is incorrect
     */
    void initiateEmailReset(EmailInitDTO emailInitDTO);

    /**
     * Resends the email verification code to the specified email address.
     * Generates a new verification code and sends it if enough time has passed
     * since the previous code was sent.
     *
     * @param emailResendDTO Data transfer object containing username and email
     * @throws UserNotFoundException if user doesn't exist
     * @throws VerificationCodeNotFoundException if no existing code is found
     * @throws CodeRequestTooSoonException if request is made too soon after previous code
     */
    void resendMailCode(EmailResendDTO emailResendDTO);

    /**
     * Completes the email change process using the verification code.
     * Validates the provided code and updates the user's email if valid.
     *
     * @param emailConfirmDTO Data transfer object containing user ID, new email, and verification code
     * @throws UserNotFoundException if user doesn't exist
     * @throws VerificationCodeNotFoundException if verification code doesn't exist
     * @throws VerificationCodeExpiredException if verification code has expired
     * @throws InvalidVerificationCodeException if verification code is incorrect
     */
    void resetEmail(EmailConfirmDTO emailConfirmDTO);
}
