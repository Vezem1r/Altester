package com.altester.auth.service;

import com.altester.auth.models.User;
import com.altester.auth.models.enums.RolesEnum;
import com.altester.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.time.LocalDateTime;
import java.util.Hashtable;

@Service
@RequiredArgsConstructor
@Slf4j
public class LdapAuthService {

    private final UserRepository userRepository;

    public User authenticate(String username, String password) {
        String userDn = "cn=" + username + ",ou=" + username.charAt(username.length() - 1) + ",ou=USERS,o=VSB";

        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldaps://ldap.vsb.cz");
        env.put(Context.SECURITY_PRINCIPAL, userDn);
        env.put(Context.SECURITY_CREDENTIALS, password);

        try {
            DirContext ctx = new InitialDirContext(env);
            log.info("User successfully authenticated: {}", username);

            // Search for attributes
            SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            String searchFilter = "(&(objectClass=person)(cn=" + username + "))";
            String baseDn = "ou=USERS,o=VSB";
            NamingEnumeration<SearchResult> results = ctx.search(baseDn, searchFilter, searchControls);

            User user = null;

            // Retrieve attributes
            while (results.hasMore()) {
                SearchResult result = results.next();
                log.info("Retrieving attributes for user: {}", username);

                String email = result.getAttributes().get("mail").get().toString();
                String uid = result.getAttributes().get("uid").get().toString().toUpperCase();
                String givenName = result.getAttributes().get("givenName").get().toString();
                String surname = result.getAttributes().get("sn").get().toString();

                user = userRepository.findByUsername(uid).orElse(null);

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
                    log.info("New user {} added to the database.", username);
                } else {
                    user.setLastLogin(LocalDateTime.now());
                    userRepository.save(user);
                }
            }

            ctx.close();
            return user;
        } catch (Exception e) {
            log.error("Authentication failed for user {}: {}", username, e.getMessage());
            return null;
        }
    }
}
