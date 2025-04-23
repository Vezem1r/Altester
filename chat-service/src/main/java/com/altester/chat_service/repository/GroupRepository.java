package com.altester.chat_service.repository;

import com.altester.chat_service.model.Group;
import com.altester.chat_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {

    @Query("SELECT s FROM User s JOIN Group g ON s MEMBER OF g.students " +
            "WHERE g.active = true AND g.teacher.username = :teacherUsername")
    List<User> findStudentsForTeacher(@Param("teacherUsername") String teacherUsername);

    @Query("SELECT t.teacher FROM Group t " +
            "WHERE t.active = true AND EXISTS (SELECT 1 FROM t.students s WHERE s.username = :studentUsername)")
    List<User> findTeachersForStudent(@Param("studentUsername") String studentUsername);

    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END FROM Group g " +
            "WHERE g.active = true AND g.teacher.username = :teacherUsername " +
            "AND EXISTS (SELECT 1 FROM g.students s WHERE s.username = :studentUsername)")
    boolean isStudentInTeacherGroup(@Param("teacherUsername") String teacherUsername,
                                    @Param("studentUsername") String studentUsername);
}