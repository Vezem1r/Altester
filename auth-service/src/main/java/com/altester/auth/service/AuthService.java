package com.altester.auth.service;

import com.altester.auth.dto.Auth.LoginResponse;
import com.altester.auth.dto.Auth.LoginUserDTO;
import com.altester.auth.dto.Auth.RegisterUserDTO;
import com.altester.auth.dto.Auth.VerifyUserDTO;

public interface AuthService {
    void register(RegisterUserDTO registerUserDTO);
    LoginResponse signIn(LoginUserDTO loginUserDTO);
    void verifyUser(VerifyUserDTO verifyUserDto);
    void resendVerificationCode(String email);
}