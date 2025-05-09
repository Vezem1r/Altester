package com.altester.core.repository;

import com.altester.core.model.ApiKey.Prompt;
import com.altester.core.model.auth.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PromptRepository extends JpaRepository<Prompt, Long> {
  Page<Prompt> findByAuthor(User author, Pageable pageable);

  @Query("SELECT p FROM Prompt p WHERE p.isPublic = true")
  Page<Prompt> findAllIsPublicTrue(Pageable pageable);

  long countByAuthor(User author);
}
