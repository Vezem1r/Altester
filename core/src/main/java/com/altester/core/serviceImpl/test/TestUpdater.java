package com.altester.core.serviceImpl.test;

import com.altester.core.dtos.core_service.test.CreateTestDTO;
import com.altester.core.model.subject.Test;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TestUpdater {
    private final Test test;
    private  final CreateTestDTO dto;

    public static  TestUpdater of(Test test, CreateTestDTO dto) {
        return new TestUpdater(test, dto);
    }

    public TestUpdater updateAllFields() {
        return updateTitle()
                .updateDescription()
                .updateDuration()
                .updateOpen()
                .updateMaxAttempts()
                .updateMaxQuestions()
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

    public TestUpdater updateOpen() {
        test.setOpen(dto.isOpen());
        return this;
    }

    public TestUpdater updateMaxAttempts() {
        if (dto.getMaxAttempts() != null) {
            test.setMaxAttempts(dto.getMaxAttempts());
        }
        return this;
    }

    public TestUpdater updateMaxQuestions() {
        if (dto.getMaxQuestions() != null) {
            test.setMaxQuestions(dto.getMaxQuestions());
        }
        return this;
    }

    public TestUpdater updateStartTime() {
        if (dto.getStartTime() != null) {
            test.setStartTime(dto.getStartTime());
        }
        return this;
    }

    public TestUpdater updateEndTime() {
        if (dto.getEndTime() != null) {
            test.setEndTime(dto.getEndTime());
        }
        return this;
    }

    public Test build() {
        return test;
    }
}
