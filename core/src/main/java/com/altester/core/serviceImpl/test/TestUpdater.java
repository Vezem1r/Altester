package com.altester.core.serviceImpl.test;

import com.altester.core.dtos.core_service.test.CreateTestDTO;
import com.altester.core.model.subject.Test;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TestUpdater {
  private final Test test;
  private final CreateTestDTO dto;

  public static TestUpdater of(Test test, CreateTestDTO dto) {
    return new TestUpdater(test, dto);
  }

  public TestUpdater updateAllFields() {
    return updateTitle()
        .updateDescription()
        .updateDuration()
        .updateMaxAttempts()
        .updateDifficultyCounts()
        .updateStartTime()
        .updateEndTime();
  }

  public TestUpdater updateTitle() {
    if (dto.getTitle() != null) {
      test.setTitle(dto.getTitle());
    }
    return this;
  }

  public TestUpdater updateDescription() {
    if (dto.getDescription() != null) {
      test.setDescription(dto.getDescription());
    }
    return this;
  }

  public TestUpdater updateDuration() {
    if (dto.getDuration() > 0) {
      test.setDuration(dto.getDuration());
    }
    return this;
  }

  public TestUpdater updateMaxAttempts() {
    test.setMaxAttempts(dto.getMaxAttempts());
    return this;
  }

  public TestUpdater updateDifficultyCounts() {
    if (dto.getEasyQuestionsCount() != null) {
      test.setEasyQuestionsCount(dto.getEasyQuestionsCount());
    }
    if (dto.getMediumQuestionsCount() != null) {
      test.setMediumQuestionsCount(dto.getMediumQuestionsCount());
    }
    if (dto.getHardQuestionsCount() != null) {
      test.setHardQuestionsCount(dto.getHardQuestionsCount());
    }
    return this;
  }

  public TestUpdater updateStartTime() {
    test.setStartTime(dto.getStartTime());
    return this;
  }

  public TestUpdater updateEndTime() {
    test.setEndTime(dto.getEndTime());

    return this;
  }

  public Test build() {
    return test;
  }
}
