package com.altester.core.serviceImpl.question;

import com.altester.core.dtos.core_service.test.OptionDTO;
import com.altester.core.exception.ValidationException;
import com.altester.core.model.subject.enums.QuestionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class QuestionValidator {

    /**
     * Validates question data based on the specified question type.
     *
     * @param questionType The type of question
     * @param questionText The text of the question
     * @param hasImage Whether the question has an image
     * @param options The options for the question (for multiple choice questions)
     * @throws ValidationException If the question data is invalid for the specified type
     */
    public void validateQuestionData(QuestionType questionType, String questionText,
                                     boolean hasImage, List<OptionDTO> options) {
        if (questionType == null) {
            log.warn("Question type not provided");
            throw ValidationException.invalidQuestionType("Question type must be provided");
        }

        boolean hasText = questionText != null && !questionText.trim().isEmpty();
        boolean hasOptions = options != null && !options.isEmpty();

        log.debug("Validating question data - type: {}, hasText: {}, hasImage: {}, hasOptions: {}",
                questionType, hasText, hasImage, hasOptions);

        switch (questionType) {
            case TEXT_ONLY:
                validateTextOnly(hasText, hasImage);
                break;

            case IMAGE_ONLY:
                validateImageOnly(hasText, hasImage);
                break;

            case TEXT_WITH_IMAGE:
                validateTextWithImage(hasText, hasImage);
                break;

            case MULTIPLE_CHOICE:
                validateMultipleChoice(hasText, hasOptions, options);
                break;

            case IMAGE_WITH_MULTIPLE_CHOICE:
                validateImageWithMultipleChoice(hasImage, hasOptions, options);
                break;

            default:
                throw ValidationException.invalidQuestionType("Unsupported question type: " + questionType);
        }
    }

    private void validateTextOnly(boolean hasText, boolean hasImage) {
        if (!hasText) {
            throw ValidationException.invalidQuestionText("TEXT_ONLY question type requires non-empty text");
        }
        if (hasImage) {
            throw ValidationException.invalidImage("TEXT_ONLY question type cannot have an image");
        }
    }

    private void validateImageOnly(boolean hasText, boolean hasImage) {
        if (!hasImage) {
            throw ValidationException.invalidImage("IMAGE_ONLY question type requires an image");
        }
        if (hasText) {
            throw ValidationException.invalidQuestionText("IMAGE_ONLY question type should not have text");
        }
    }

    private void validateTextWithImage(boolean hasText, boolean hasImage) {
        if (!hasText) {
            throw ValidationException.invalidQuestionText("TEXT_WITH_IMAGE question type requires non-empty text");
        }
        if (!hasImage) {
            throw ValidationException.invalidImage("TEXT_WITH_IMAGE question type requires an image");
        }
    }

    private void validateMultipleChoice(boolean hasText, boolean hasOptions, List<OptionDTO> options) {
        if (!hasText) {
            throw ValidationException.invalidQuestionText("MULTIPLE_CHOICE question type requires non-empty text");
        }
        if (!hasOptions) {
            throw ValidationException.invalidOption("MULTIPLE_CHOICE question type requires at least one option");
        }

        validateOptionsHaveCorrectAnswer(options);
    }

    private void validateImageWithMultipleChoice(boolean hasImage, boolean hasOptions, List<OptionDTO> options) {
        if (!hasImage) {
            throw ValidationException.invalidImage("IMAGE_WITH_MULTIPLE_CHOICE question type requires an image");
        }
        if (!hasOptions) {
            throw ValidationException.invalidOption("IMAGE_WITH_MULTIPLE_CHOICE question type requires at least one option");
        }

        validateOptionsHaveCorrectAnswer(options);
    }

    private void validateOptionsHaveCorrectAnswer(List<OptionDTO> options) {
        boolean hasCorrectOption = options.stream().anyMatch(OptionDTO::isCorrect);
        if (!hasCorrectOption) {
            throw ValidationException.missingCorrectOption("Question must have at least one correct option");
        }
    }
}