package com.altester.auth.repository;

import com.altester.auth.models.Codes;
import com.altester.auth.models.User;
import com.altester.auth.models.enums.CodeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CodeRepository extends JpaRepository<Codes, Long> {

    Optional<Codes> findByUserAndCodeType(User user, CodeType codeType);

    Optional<Codes> findByCodeAndCodeType(String code, CodeType codeType);

    List<Codes> findAllByUser(User user);

    @Modifying
    @Transactional
    @Query("DELETE FROM Codes c WHERE c.expiration < :now")
    int deleteByExpirationBefore(LocalDateTime now);
}
