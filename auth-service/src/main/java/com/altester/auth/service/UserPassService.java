package com.altester.auth.service;

import com.altester.auth.dto.ChangePassDTO;

public interface UserPassService {
    void initiatePasswordReset(String email);
    void resetPassword(ChangePassDTO changePassDTO);
    void resendResetCode(String email);
}