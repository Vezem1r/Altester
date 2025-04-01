package com.altester.core.dtos.core_service.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionReviewDTO {
    private long questionId;
    private String questionText;
    private String imagePath;
    private List<OptionReviewDTO> options;
    private String studentAnswer;
    private List<Long> selectedOptionIds;
    private int score;
    private int maxScore;
    private String teacherFeedback;
}