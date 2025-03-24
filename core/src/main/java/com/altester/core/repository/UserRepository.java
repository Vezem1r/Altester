package com.altester.core.repository;

import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

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

    @Query(value = "SELECT u FROM User u WHERE u.role = :role " +
            "AND u.id NOT IN :groupStudentsIds " +
            "AND u.id NOT IN :subjectStudentsIds " +
            "ORDER BY u.name, u.surname")
    Page<User> findAllByRoleExcludeGroupAndSubjectStudents(
            @Param("role") String role,
            @Param("groupStudentsIds") Set<Long> groupStudentsIds,
            @Param("subjectStudentsIds") Set<Long> subjectStudentsIds,
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
