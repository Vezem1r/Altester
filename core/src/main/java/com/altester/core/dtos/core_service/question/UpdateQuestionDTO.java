package com.altester.core.dtos.core_service.question;

import com.altester.core.dtos.core_service.test.OptionDTO;
import com.altester.core.model.subject.enums.QuestionType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateQuestionDTO {

    @NotBlank(message = "Question text is required")
    @Size(max = 2000, message = "Question text must be less than 2000 characters")
    private String questionText;

    @Min(value = 1, message = "Score must be at least 1")
    @Max(value = 100, message = "Score cannot exceed 100")
    private int score;

    @NotNull(message = "Question type is required")
    private QuestionType questionType;

    private boolean removeImage;

    @Size(max = 1000, message = "Correct answer must be less than 1000 characters")
    private String correctAnswer;

    private List<OptionDTO> options;
}
