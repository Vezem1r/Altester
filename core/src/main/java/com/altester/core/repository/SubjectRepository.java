package com.altester.core.repository;

import com.altester.core.model.subject.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    Optional<Subject> findByShortName(String shortName);
    Page<Subject> findAll(Pageable pageable);
    Optional<Subject> findByGroupsId(Long groupsId);
}
