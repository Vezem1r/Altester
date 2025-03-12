package com.altester.core.repository;

import com.altester.core.model.subject.Test;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestRepository  extends JpaRepository<Test, Long> {

}
