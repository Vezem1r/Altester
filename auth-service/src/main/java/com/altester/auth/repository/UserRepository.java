package com.altester.auth.repository;

import com.altester.auth.models.User;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
  Optional<User> findByEmail(String email);

  Optional<User> findByUsername(String username);
}
