package com.altester.core.dtos.core_service.question;

import com.altester.core.dtos.core_service.test.OptionDTO;
import com.altester.core.model.subject.enums.QuestionDifficulty;
import com.altester.core.model.subject.enums.QuestionType;
import jakarta.validation.constraints.*;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateQuestionDTO {

  @NotBlank(message = "Question text is required")
  @Size(max = 2000, message = "Question text must be less than 2000 characters")
  private String questionText;

  @NotNull(message = "Question type is required")
  private QuestionType questionType;

  private QuestionDifficulty difficulty;

  @Size(max = 1000, message = "Correct answer must be less than 1000 characters")
  private String correctAnswer;

  private List<OptionDTO> options;

  private boolean removeImage;
}
