package com.altester.auth.repository;

import com.altester.auth.models.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);

    @Query("SELECT CAST(SUBSTRING(u.username, 4) AS int) FROM User u WHERE u.username LIKE ?1% ORDER BY CAST(SUBSTRING(u.username, 4) AS int)")
    List<Integer> findUsedUsernameNumbers(String prefix);
}
