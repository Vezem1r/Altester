package com.altester.core.dtos.core_service.question;

import com.altester.core.dtos.core_service.test.OptionDTO;
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
public class QuestionDetailsDTO {
    private long id;
    private String questionText;
    private String imagePath;
    private int score;
    private QuestionType questionType;
    private QuestionDifficulty difficulty;
    private String correctAnswer;
    private List<OptionDTO> options;
    private int position;
}
