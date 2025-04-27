package com.altester.core.repository;

import com.altester.core.model.ApiKey.TestGroupAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestGroupAssignmentRepository extends JpaRepository<TestGroupAssignment, Long> {
}
