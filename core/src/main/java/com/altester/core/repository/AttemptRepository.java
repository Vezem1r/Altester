package com.altester.core.repository;

import com.altester.core.model.auth.User;
import com.altester.core.model.subject.Attempt;
import com.altester.core.model.subject.Test;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttemptRepository extends JpaRepository<Attempt, Long> {
    List<Attempt> findByTestAndStudent(Test test, User student);
    int countByEndTimeAfter(LocalDateTime date);

    @Query("SELECT a FROM Attempt a LEFT JOIN FETCH a.submissions WHERE a.id = :id")
    Optional<Attempt> findByIdWithSubmissions(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Attempt a LEFT JOIN FETCH a.submissions WHERE a.id = :id")
    Optional<Attempt> findByIdWithSubmissionsAndLock(@Param("id") Long id);
}
