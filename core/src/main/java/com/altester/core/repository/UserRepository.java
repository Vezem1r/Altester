package com.altester.core.repository;

import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Page<User> findByRole(RolesEnum role, Pageable pageable);
    Long countByRole(RolesEnum role);
}
