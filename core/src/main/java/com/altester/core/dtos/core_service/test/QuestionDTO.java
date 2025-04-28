package com.altester.core.dtos.core_service.test;

import com.altester.core.model.subject.enums.QuestionDifficulty;
import com.altester.core.model.subject.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionDTO {
    private Long id;
    private String questionText;
    private String imagePath;
    private int score;
    private int position;
    private QuestionType questionType;
    private QuestionDifficulty difficulty;
    private String correctAnswer;
    private List<OptionDTO> options;
}