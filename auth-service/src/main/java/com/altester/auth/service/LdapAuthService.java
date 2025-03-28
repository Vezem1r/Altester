package com.altester.auth.service;

import com.altester.auth.dto.Auth.LoginResponse;
import com.altester.auth.dto.LdapLoginRequest;
import com.altester.auth.models.User;


public interface LdapAuthService {
    User authenticate(String username, String password);
    LoginResponse login(LdapLoginRequest request);
}