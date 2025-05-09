package com.altester.core.repository;

import com.altester.core.model.subject.Test;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TestRepository extends JpaRepository<Test, Long> {

  @Query(
      "SELECT DISTINCT t FROM Test t WHERE "
          + "(:searchQuery IS NULL OR :searchQuery = '' OR "
          + "LOWER(t.title) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR "
          + "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchQuery, '%'))) "
          + "AND (:isActive IS NULL OR t.isOpen = :isActive)")
  Page<Test> findAllWithFilters(
      @Param("searchQuery") String searchQuery,
      @Param("isActive") Boolean isActive,
      Pageable pageable);

  @Query(
      "SELECT DISTINCT t FROM Test t JOIN Group g ON t MEMBER OF g.tests WHERE "
          + "g.teacher.id = :teacherId "
          + "AND (:searchQuery IS NULL OR :searchQuery = '' OR "
          + "LOWER(t.title) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR "
          + "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchQuery, '%'))) "
          + "AND (:isActive IS NULL OR t.isOpen = :isActive) "
          + "AND (:allowTeacherEdit IS NULL OR t.allowTeacherEdit = :allowTeacherEdit) "
          + "ORDER BY t.allowTeacherEdit DESC")
  Page<Test> findByTeacherWithFilters(
      @Param("teacherId") Long teacherId,
      @Param("searchQuery") String searchQuery,
      @Param("isActive") Boolean isActive,
      @Param("allowTeacherEdit") Boolean allowTeacherEdit,
      Pageable pageable);

  @Query(
      "SELECT DISTINCT t FROM Test t JOIN Group g ON t MEMBER OF g.tests JOIN Subject s ON g MEMBER OF s.groups "
          + "WHERE s.id = :subjectId "
          + "AND (:searchQuery IS NULL OR :searchQuery = '' OR "
          + "LOWER(t.title) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR "
          + "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchQuery, '%'))) "
          + "AND (:isActive IS NULL OR t.isOpen = :isActive)")
  Page<Test> findBySubjectWithFilters(
      @Param("subjectId") Long subjectId,
      @Param("searchQuery") String searchQuery,
      @Param("isActive") Boolean isActive,
      Pageable pageable);

  @Query(
      "SELECT DISTINCT t FROM Test t JOIN Group g ON t MEMBER OF g.tests "
          + "WHERE g.id = :groupId "
          + "AND (:searchQuery IS NULL OR :searchQuery = '' OR "
          + "LOWER(t.title) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR "
          + "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchQuery, '%'))) "
          + "AND (:isActive IS NULL OR t.isOpen = :isActive)")
  Page<Test> findByGroupWithFilters(
      @Param("groupId") Long groupId,
      @Param("searchQuery") String searchQuery,
      @Param("isActive") Boolean isActive,
      Pageable pageable);

  @Query("SELECT COUNT(t) FROM Test t WHERE t.isOpen = true")
  int countOpenTests();

  @Query(
      "SELECT t FROM Test t LEFT JOIN FETCH t.questions WHERE t.isOpen = false AND (t.questions IS EMPTY OR SIZE(t.questions) = 0)")
  List<Test> findAllNonOpenTestsWithoutQuestions();
}
