package com.altester.core.dtos.core_service.attempt;

import com.altester.core.model.subject.enums.QuestionType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {
  private long id;
  private String questionText;
  private String imagePath;
  private int score;
  private int position;
  private QuestionType questionType;
  private List<OptionDTO> options;
}
