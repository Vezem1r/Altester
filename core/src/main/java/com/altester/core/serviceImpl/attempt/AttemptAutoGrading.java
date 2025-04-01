package com.altester.core.serviceImpl.attempt;

import com.altester.core.model.subject.Option;
import com.altester.core.model.subject.Question;
import com.altester.core.model.subject.Submission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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

        boolean allCorrectSelected = allOptions.stream()
                .filter(Option::isCorrect)
                .allMatch(selectedOptions::contains);

        boolean anyIncorrectSelected = selectedOptions.stream()
                .anyMatch(option -> !option.isCorrect());

        if (allCorrectSelected && !anyIncorrectSelected) {
            submission.setScore(question.getScore());
            return question.getScore();
        } else {
            submission.setScore(0);
            return 0;
        }
    }
}
