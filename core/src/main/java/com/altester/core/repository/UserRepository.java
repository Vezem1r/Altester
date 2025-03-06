package com.altester.core.repository;

import com.altester.core.model.auth.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.groups g " +
            "LEFT JOIN FETCH u.taughtGroups tg " +
            "WHERE u.username = :username")
    Optional<User> findByUsernameWithGroups(@Param("username") String username);

    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.groups g " +
            "LEFT JOIN FETCH u.taughtGroups tg " +
            "WHERE u.email = :email")
    Optional<User> findByEmailWithGroups(@Param("email") String email);
}
