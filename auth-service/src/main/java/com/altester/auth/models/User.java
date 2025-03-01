package com.altester.auth.models;

import com.altester.auth.models.enums.RolesEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;

@Entity
@Data
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 63, nullable = false)
    private String name;

    @Column(length = 127, nullable = false)
    private String surname;

    @Column(length = 63, nullable = false, unique = true)
    private String email;

    @Column(length = 7, nullable = false, unique = true)
    private String username;

    @Column(length = 127)
    @JsonIgnore
    private String password;

    @Column
    private LocalDateTime created;

    @Column
    private LocalDateTime lastLogin;

    @Enumerated(EnumType.STRING)
    private RolesEnum role;

    @Column
    private boolean isRegistered;

    /*-----------------------------------------------------*/

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getAuthorities();
    }

    @JsonIgnore
    private boolean enabled;

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
