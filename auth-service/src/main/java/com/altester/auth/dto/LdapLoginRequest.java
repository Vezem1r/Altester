package com.altester.auth.dto;

import lombok.Data;

@Data
public class LdapLoginRequest {
    private String login;
    private String password;
}
