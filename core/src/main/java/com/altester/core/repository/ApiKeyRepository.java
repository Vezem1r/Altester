package com.altester.core.repository;

import com.altester.core.model.ApiKey.ApiKey;
import com.altester.core.model.auth.User;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

  @Query("SELECT k FROM ApiKey k WHERE k.isGlobal = true OR k.owner = :user")
  List<ApiKey> findAllGlobalOrOwnedBy(@Param("user") User user);

  Optional<ApiKey> findByName(String name);
}
