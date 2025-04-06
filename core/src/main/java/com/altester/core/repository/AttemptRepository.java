package com.altester.core.repository;

import com.altester.core.model.auth.User;
import com.altester.core.model.subject.Attempt;
import com.altester.core.model.subject.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttemptRepository extends JpaRepository<Attempt, Long> {
    List<Attempt> findByTestAndStudent(Test test, User student);
    int countByEndTimeAfter(LocalDateTime date);
}
