package com.altester.core.repository;

import com.altester.core.model.ApiKey.ApiKey;
import com.altester.core.model.ApiKey.TestGroupAssignment;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Test;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestGroupAssignmentRepository extends JpaRepository<TestGroupAssignment, Long> {
  Optional<TestGroupAssignment> findByTestAndGroup(Test test, Group group);

  List<TestGroupAssignment> findByApiKey(ApiKey apiKey);
}
