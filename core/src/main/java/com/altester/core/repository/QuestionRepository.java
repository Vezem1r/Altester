package com.altester.core.repository;

import com.altester.core.model.subject.Question;
import com.altester.core.model.subject.Test;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
  List<Question> findByTest(Test test);
}
