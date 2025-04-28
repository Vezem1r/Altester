package com.altester.core.serviceImpl.question;

import com.altester.core.dtos.core_service.question.QuestionDetailsDTO;
import com.altester.core.dtos.core_service.test.OptionDTO;
import com.altester.core.model.subject.Option;
import com.altester.core.model.subject.Question;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class QuestionDTOMapper {

    /**
     * Converts an Option entity to an OptionDTO.
     *
     * @param option The Option entity to convert
     * @return OptionDTO representation of the Option entity
     */
    public OptionDTO convertToOptionDTO(Option option) {
        return OptionDTO.builder()
                .id(option.getId())
                .text(option.getText())
                .description(option.getDescription())
                .isCorrect(option.isCorrect())
                .build();
    }

    /**
     * Converts a Question entity to a QuestionDetailsDTO, including its options.
     *
     * @param question The Question entity to convert
     * @return QuestionDetailsDTO representation of the Question entity
     */
    public QuestionDetailsDTO convertToQuestionDetailsDTO(Question question) {
        List<OptionDTO> options = question.getOptions() != null
                ? question.getOptions().stream()
                .map(this::convertToOptionDTO)
                .collect(Collectors.toList())
                : Collections.emptyList();

        return QuestionDetailsDTO.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .imagePath(question.getImagePath())
                .score(question.getScore())
                .questionType(question.getQuestionType())
                .difficulty(question.getDifficulty())
                .correctAnswer(question.getCorrectAnswer())
                .options(options)
                .build();
    }
}