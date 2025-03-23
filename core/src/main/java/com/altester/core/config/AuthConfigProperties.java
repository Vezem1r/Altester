package com.altester.core.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class AuthConfigProperties {

    /**
     * Authentication mode determines which authentication methods are available.
     * Possible values:
     * - ALL: All methods (LDAP + standard login + registration)
     * - STANDARD_ONLY: Only standard authentication (login + registration always together)
     * - LDAP_ONLY: Only LDAP authentication
     */
    @Value("${auth.mode:ALL}")
    private String modeString;

    public enum AuthMode {
        ALL,
        STANDARD_ONLY,
        LDAP_ONLY
    }

    public AuthMode getMode() {
        try {
            return AuthMode.valueOf(modeString);
        } catch (IllegalArgumentException e) {
            return AuthMode.ALL;
        }
    }

    public boolean isStandardAuthEnabled() {
        return getMode() == AuthMode.ALL || getMode() == AuthMode.STANDARD_ONLY;
    }

    public boolean isRegistrationEnabled() {
        // Registration is always tied to standard auth mode
        return isStandardAuthEnabled();
    }

    public boolean isLdapAuthEnabled() {
        return getMode() == AuthMode.ALL || getMode() == AuthMode.LDAP_ONLY;
    }
}