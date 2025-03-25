package com.altester.core.dtos.core_service.question;

import com.altester.core.dtos.core_service.test.OptionDTO;
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
public class CreateQuestionDTO {
    private String questionText;
    private int score;
    private QuestionType questionType;
    private List<OptionDTO> options;
}
