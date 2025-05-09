package com.altester.core.repository;

import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Subject;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

  Optional<Subject> findByShortName(String shortName);

  Page<Subject> findAll(Pageable pageable);

  Optional<Subject> findByGroupsId(Long groupsId);

  Optional<Subject> findByGroupsContaining(Group group);

  Page<Subject> findByNameContainingIgnoreCaseOrShortNameContainingIgnoreCase(
      String name, String shortName, Pageable pageable);
}
