package com.altester.auth.service;

import com.altester.auth.dto.EmailConfirmDTO;
import com.altester.auth.dto.EmailInitDTO;
import com.altester.auth.dto.EmailResendDTO;

public interface UserEmailService {
    void initiateEmailReset(EmailInitDTO emailInitDTO);
    void resendMailCode(EmailResendDTO emailResendDTO);
    void resetEmail(EmailConfirmDTO emailConfirmDTO);
}
