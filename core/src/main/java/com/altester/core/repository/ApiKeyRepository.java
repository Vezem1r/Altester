package com.altester.core.repository;

import com.altester.core.model.auth.User;
import com.altester.core.model.subject.ApiKey;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    @Query("SELECT k FROM ApiKey k WHERE k.isGlobal = true OR k.owner = :user")
    List<ApiKey> findAllGlobalOrOwnedBy(@Param("user")User user);

    List<ApiKey> findByOwner(User user);
    List<ApiKey> findByIsGlobalTrue();

    @Query("SELECT k FROM ApiKey k WHERE k.id = :id AND (k.isGlobal = true OR k.owner = :user)")
    ApiKey findByIdAndGlobalOrOwnedBy(@Param("id") Long id, @Param("user") User user);
}
