package com.altester.core.dtos.auth_service.auth;

import lombok.Data;

@Data
public class LdapLoginDTO {
    private String login;
    private String password;
}
