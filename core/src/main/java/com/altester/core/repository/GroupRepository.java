package com.altester.core.repository;

import com.altester.core.model.auth.User;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByName(String name);
    List<Group> findByTeacher(User teacher);
    List<Group> findAllByStudentsContaining(User student);
    List<Group> findAllByTeacher(User teacher);
    List<Group> findByStudentsContainingAndActiveTrue(User student);

    @Query("SELECT g FROM Group g JOIN g.students s WHERE s.id = :studentId")
    List<Group> findAllByStudentId(@Param("studentId") Long studentId);

    List<Group> findByTeacherAndTestsContaining(User currentUser, Test test);
}
