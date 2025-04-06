package com.altester.core.repository;

import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Page<User> findByRole(RolesEnum role, Pageable pageable);
    Long countByRole(RolesEnum role);
    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findAllByRole(@Param("role") RolesEnum role);
    int countByLastLoginAfter(LocalDateTime date);
}
