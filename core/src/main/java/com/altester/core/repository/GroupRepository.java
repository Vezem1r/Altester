package com.altester.core.repository;

import com.altester.core.model.auth.User;
import com.altester.core.model.subject.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByName(String name);
    List<Group> findByTeacher(User teacher);
    List<Group> findAllByStudentsContaining(User student);
    List<Group> findAllByTeacher(User teacher);
    List<Group> findByStudentsContainingAndActiveTrue(User student);
}
