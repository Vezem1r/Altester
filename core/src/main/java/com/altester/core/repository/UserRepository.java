package com.altester.core.repository;

import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Page<User> findByRole(RolesEnum role, Pageable pageable);
    Long countByRole(RolesEnum role);
    Page<User> findAllByRole(RolesEnum role, Pageable pageable);

    @Query(nativeQuery = true, value =
            "SELECT * FROM users u WHERE u.role = :role " +
                    "ORDER BY CASE WHEN u.id IN :studentsInSubject THEN 1 ELSE 0 END, u.surname, u.name")
    Page<User> findAllByRoleOrderBySubjectMembership(
            @Param("role") String role,
            @Param("studentsInSubject") Set<Long> studentsInSubject,
            Pageable pageable);

    @Query(nativeQuery = true, value =
            "SELECT * FROM users u WHERE u.role = :role AND u.id NOT IN :excludedIds " +
                    "ORDER BY " +
                    "CASE WHEN u.id IN :subjectStudentIds THEN 1 ELSE 0 END, " +
                    "u.surname, u.name")
    Page<User> findAllByRoleExcludeGroupStudentsOrderBySubjectMembership(
            @Param("role") String role,
            @Param("excludedIds") Set<Long> excludedIds,
            @Param("subjectStudentIds") Set<Long> subjectStudentIds,
            Pageable pageable);
}
