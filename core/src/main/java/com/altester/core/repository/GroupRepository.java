package com.altester.core.repository;

import com.altester.core.model.subject.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByName(String name);
    Optional<Group> findByTests_id(Long test_id);
}
