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
import java.util.Set;

public interface UserRepository extends CrudRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Page<User> findByRole(RolesEnum role, Pageable pageable);
    Long countByRole(RolesEnum role);
    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findAllByRole(@Param("role") RolesEnum role);
    int countByLastLoginAfter(LocalDateTime date);

    @Query("SELECT u FROM User u WHERE u.role = :role AND " +
            "(LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.surname) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<User> findByRoleAndSearchTerm(
            @Param("role") RolesEnum role,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.id NOT IN :excludedIds")
    Page<User> findByRoleAndIdNotIn(
            @Param("role") RolesEnum role,
            @Param("excludedIds") Set<Long> excludedIds,
            Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.id NOT IN :excludedIds AND " +
            "(LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.surname) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<User> findByRoleAndIdNotInAndSearchTerm(
            @Param("role") RolesEnum role,
            @Param("excludedIds") Set<Long> excludedIds,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);
}
