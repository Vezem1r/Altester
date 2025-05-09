package com.altester.core.serviceImpl.attempt;

import com.altester.core.model.subject.Option;
import com.altester.core.model.subject.Question;
import com.altester.core.model.subject.Submission;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttemptAutoGrading {

  public int gradeMultipleSelectionQuestion(Submission submission) {
    Question question = submission.getQuestion();

    if (submission.getSelectedOptions() == null || submission.getSelectedOptions().isEmpty()) {
      submission.setScore(0);
      return 0;
    }

    List<Option> selectedOptions = submission.getSelectedOptions();
    List<Option> allOptions = question.getOptions();

    List<Option> correctOptions = allOptions.stream().filter(Option::isCorrect).toList();

    List<Option> selectedCorrectOptions =
        selectedOptions.stream().filter(Option::isCorrect).toList();

    List<Option> selectedIncorrectOptions =
        selectedOptions.stream().filter(option -> !option.isCorrect()).toList();

    int totalCorrectOptions = correctOptions.size();

    if (selectedCorrectOptions.size() == totalCorrectOptions
        && selectedIncorrectOptions.isEmpty()) {
      submission.setScore(question.getScore());
      return question.getScore();
    }

    if (!selectedCorrectOptions.isEmpty() && selectedCorrectOptions.size() < totalCorrectOptions) {
      double partialScore =
          (double) selectedCorrectOptions.size() / totalCorrectOptions * question.getScore();

      if (!selectedIncorrectOptions.isEmpty()) {
        partialScore = 0;
      }

      int finalScore = (int) Math.floor(partialScore);
      submission.setScore(finalScore);
      return finalScore;
    }
    submission.setScore(0);
    return 0;
  }
}
