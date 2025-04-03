package com.altester.auth.serviceImpl;

import com.altester.auth.dto.Auth.LoginResponse;
import com.altester.auth.dto.LdapLoginRequest;
import com.altester.auth.exception.LdapAuthException;
import com.altester.auth.models.User;
import com.altester.auth.models.enums.RolesEnum;
import com.altester.auth.repository.UserRepository;
import com.altester.auth.service.LdapAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.time.LocalDateTime;
import java.util.Hashtable;

@Service
@RequiredArgsConstructor
@Slf4j
public class LdapAuthServiceImpl implements LdapAuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    public LoginResponse login(LdapLoginRequest request) {
        log.info("Processing LDAP login for user: {}", request.getLogin());

        User authenticatedUser = authenticate(request.getLogin(), request.getPassword());

        log.info("User {} authenticated successfully, generating token", request.getLogin());

        String token = jwtService.generateToken(authenticatedUser, authenticatedUser.getRole().name(), false);

        return new LoginResponse(token, authenticatedUser.getRole().toString(),"Login successful");
    }

    @Override
    public User authenticate(String username, String password) {
        if (username == null || username.isEmpty()) {
            log.error("Username cannot be empty");
            throw new LdapAuthException("", "Username cannot be empty");
        }

        if (password == null || password.isEmpty()) {
            log.error("Password cannot be empty for user: {}", username);
            throw new LdapAuthException(username, "Password cannot be empty");
        }

        String userDn = "cn=" + username + ",ou=" + username.charAt(username.length() - 1) + ",ou=USERS,o=VSB";

        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldaps://ldap.vsb.cz");
        env.put(Context.SECURITY_PRINCIPAL, userDn);
        env.put(Context.SECURITY_CREDENTIALS, password);

        DirContext ctx = null;
        try {
            ctx = new InitialDirContext(env);
            log.info("User successfully authenticated: {}", username);

            return getUserAttributes(ctx, username);

        } catch (AuthenticationException e) {
            log.error("Invalid credentials for user {}", username);
            throw new LdapAuthException(username, "Invalid credentials");
        } catch (CommunicationException e) {
            log.error("Failed to connect to LDAP server for user {}: {}", username, e.getMessage());
            throw new LdapAuthException(username, "LDAP server connection failed");
        } catch (NamingException e) {
            log.error("LDAP error for user {}: {}", username, e.getMessage());
            throw new LdapAuthException(username, "LDAP error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during LDAP authentication for user {}: {}", username, e.getMessage());
            throw new LdapAuthException(username, "Unexpected error: " + e.getMessage());
        } finally {
            closeContext(ctx);
        }
    }

    private User getUserAttributes(DirContext ctx, String username) throws NamingException {
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String searchFilter = "(&(objectClass=person)(cn=" + username + "))";
        String baseDn = "ou=USERS,o=VSB";

        NamingEnumeration<SearchResult> results = ctx.search(baseDn, searchFilter, searchControls);

        if (!results.hasMore()) {
            log.error("User authenticated but no attributes found: {}", username);
            throw new LdapAuthException(username, "User attributes not found");
        }

        SearchResult result = results.next();
        log.info("Retrieving attributes for user: {}", username);

        try {
            String email = result.getAttributes().get("mail").get().toString();
            String uid = result.getAttributes().get("uid").get().toString().toUpperCase();
            String givenName = result.getAttributes().get("givenName").get().toString();
            String surname = result.getAttributes().get("sn").get().toString();

            return createOrUpdateUser(uid, email, givenName, surname);
        } catch (NamingException e) {
            log.error("Required attribute missing for user {}: {}", username, e.getMessage());
            throw new LdapAuthException(username, "Required attribute missing: " + e.getMessage());
        }
    }

    private User createOrUpdateUser(String uid, String email, String givenName, String surname) {
        User user = userRepository.findByUsername(uid).orElse(null);

        if (user == null) {
            user = User.builder()
                    .username(uid)
                    .email(email)
                    .name(givenName)
                    .surname(surname)
                    .created(LocalDateTime.now())
                    .lastLogin(LocalDateTime.now())
                    .role(RolesEnum.STUDENT)
                    .enabled(true)
                    .isRegistered(false)
                    .build();

            userRepository.save(user);
            log.info("New user {} added to the database.", uid);
        } else {
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            log.info("Existing user {} updated with new login time.", uid);
        }

        return user;
    }

    private void closeContext(DirContext ctx) {
        if (ctx != null) {
            try {
                ctx.close();
            } catch (NamingException e) {
                log.warn("Error closing LDAP context: {}", e.getMessage());
            }
        }
    }
}