package com.altester.core.service.test;

import com.altester.core.dtos.core_service.question.CreateQuestionDTO;
import com.altester.core.dtos.core_service.question.QuestionDetailsDTO;
import com.altester.core.dtos.core_service.question.UpdateQuestionDTO;
import com.altester.core.dtos.core_service.test.OptionDTO;
import com.altester.core.exception.*;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Option;
import com.altester.core.model.subject.Question;
import com.altester.core.model.subject.Test;
import com.altester.core.model.subject.enums.QuestionType;
import com.altester.core.repository.*;
import com.altester.core.service.subject.GroupActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final TestRepository testRepository;
    private final UserRepository userRepository;
    private final OptionRepository optionRepository;
    private final GroupRepository groupRepository;
    private final TestAccessValidator testAccessValidator;
    private final TestDTOMapper testDTOMapper;
    private final GroupActivityService groupActivityService;

    @Value("${app.upload.question-images")
    private String uploadDir;

    @Transactional
    public QuestionDetailsDTO addQuestion(Long testId, CreateQuestionDTO createQuestionDTO,
                                          Principal principal, MultipartFile image) {
        log.info("User {} is attempting to add a question to test with ID {}", principal.getName(), testId);

        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> {
                    log.error("User {} not found", principal.getName());
                    return new UserNotFoundException("User not found");
                });

        Test test = testRepository.findById(testId)
                .orElseThrow(() -> {
                    log.error("Test with ID {} not found", testId);
                    return new TestNotFoundException("Test not found");
                });

        verifyTestModificationPermission(currentUser, test);

        validateQuestionData(createQuestionDTO.getQuestionType(), createQuestionDTO.getQuestionText(),
                image, createQuestionDTO.getOptions());

        String imagePath = null;
        if (image != null && !image.isEmpty()) {
            try {
                imagePath = saveImage(image);
                log.info("Image saved successfully: {}", imagePath);
            } catch (Exception e) {
                log.error("Failed to save image for question", e);
                throw new ImageSaveException("Could not save image");
            }
        }

        int lastPosition = questionRepository.findMaxPositionByTestId(testId).orElse(0);

        Question question = Question.builder()
                .questionText(createQuestionDTO.getQuestionText())
                .imagePath(imagePath)
                .score(createQuestionDTO.getScore())
                .questionType(createQuestionDTO.getQuestionType())
                .test(test)
                .position(lastPosition + 1)
                .build();

        Question savedQuestion = questionRepository.save(question);

        if (createQuestionDTO.getOptions() != null && !createQuestionDTO.getOptions().isEmpty()) {
            createQuestionDTO.getOptions().forEach(optionDTO -> {
                Option option = Option.builder()
                        .text(optionDTO.getText())
                        .description(optionDTO.getDescription())
                        .isCorrect(optionDTO.isCorrect())
                        .question(savedQuestion)
                        .build();
                optionRepository.save(option);
            });
        }

        log.info("Question with ID {} added to test with ID {}", savedQuestion.getId(), testId);
        return convertToQuestionDetailsDTO(savedQuestion);
    }

    @Transactional
    public QuestionDetailsDTO updateQuestion(Long questionId, UpdateQuestionDTO updateQuestionDTO,
                                             Principal principal, MultipartFile image) {
        log.info("User {} is attempting to update question with ID {}", principal.getName(), questionId);

        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> {
                    log.error("User {} not found", principal.getName());
                    return new UserNotFoundException("User not found");
                });

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> {
                    log.error("Question with ID {} not found", questionId);
                    return new QuestionNotFoundException("Question not found");
                });

        Test test = question.getTest();

        verifyTestModificationPermission(currentUser, test);

        String imagePath = question.getImagePath();
        if (image != null && !image.isEmpty()) {
            if (imagePath != null) {
                deleteImage(imagePath);
            }
            imagePath = saveImage(image);
        } else if (updateQuestionDTO.isRemoveImage() && imagePath != null) {
            deleteImage(imagePath);
            imagePath = null;
        }

        String questionText = updateQuestionDTO.getQuestionText() != null ?
                updateQuestionDTO.getQuestionText() : question.getQuestionText();

        validateQuestionData(updateQuestionDTO.getQuestionType(), questionText,
                (image != null && !image.isEmpty()) || (imagePath != null && !updateQuestionDTO.isRemoveImage()) ?
                        new Object() : null, updateQuestionDTO.getOptions());

        if (updateQuestionDTO.getQuestionText() != null) {
            question.setQuestionText(updateQuestionDTO.getQuestionText());
        }

        if (updateQuestionDTO.getScore() > 0) {
            question.setScore(updateQuestionDTO.getScore());
        }

        question.setImagePath(imagePath);
        question.setQuestionType(updateQuestionDTO.getQuestionType());

        if (updateQuestionDTO.getOptions() != null) {
            optionRepository.deleteAll(question.getOptions());
            question.getOptions().clear();

            updateQuestionDTO.getOptions().forEach(optionDTO -> {
                Option option = Option.builder()
                        .text(optionDTO.getText())
                        .description(optionDTO.getDescription())
                        .isCorrect(optionDTO.isCorrect())
                        .question(question)
                        .build();
                optionRepository.save(option);
                question.getOptions().add(option);
            });
        }

        Question updatedQuestion = questionRepository.save(question);
        log.info("Question with ID {} updated", questionId);

        return convertToQuestionDetailsDTO(updatedQuestion);
    }

    @Transactional
    public void deleteQuestion(Long questionId, Principal principal) {
        log.info("User {} is attempting to delete question with ID {}", principal.getName(), questionId);

        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> {
                    log.error("User {} not found", principal.getName());
                    return new UserNotFoundException("User not found");
                });

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> {
                    log.error("Question with ID {} not found", questionId);
                    return new QuestionNotFoundException("Question not found");
                });

        int position = question.getPosition();
        Test test = question.getTest();

        verifyTestModificationPermission(currentUser, test);

        if (question.getImagePath() != null) {
            deleteImage(question.getImagePath());
        }

        questionRepository.delete(question);

        questionRepository.decrementPositionForRange(
                test.getId(),
                position + 1,
                Integer.MAX_VALUE
        );

        log.info("Question with ID {} deleted", questionId);
    }

    @Transactional(readOnly = true)
    public QuestionDetailsDTO getQuestion(Long questionId, Principal principal) {
        log.info("User {} is attempting to get question with ID {}", principal.getName(), questionId);

        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> {
                    log.error("User {} not found", principal.getName());
                    return new UserNotFoundException("User not found");
                });

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> {
                    log.error("Question with ID {} not found", questionId);
                    return new QuestionNotFoundException("Question not found");
                });

        Test test = question.getTest();

        testAccessValidator.validateTestAccess(currentUser, test);

        return convertToQuestionDetailsDTO(question);
    }

    private void verifyTestModificationPermission(User currentUser, Test test) {
        if (currentUser.getRole() == RolesEnum.TEACHER) {
            List<Group> teacherGroups = groupRepository.findByTeacher(currentUser);

            testAccessValidator.canTeacherEditTest(currentUser, test, teacherGroups);
        }
        if (currentUser.getRole() != RolesEnum.ADMIN) {
            log.warn("User {} with role {} attempted to modify test", currentUser.getUsername(), currentUser.getRole());
            throw new TestAccessDeniedException("Not authorized to modify this test");
        }

        List<Group> testGroups = testDTOMapper.findGroupsByTest(test);
        for (Group group : testGroups) {
            if (!groupActivityService.canModifyGroup(group)) {
                log.warn("Attempt to modify test in past semester group");
                throw new GroupInactiveException("Cannot modify questions in a test from a past semester group");
            }
        }
    }

    @Transactional
    public void changeQuestionPosition(Long questionId, int newPosition, Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException("Question not found"));

        Test test = question.getTest();

        verifyTestModificationPermission(currentUser, test);

        int oldPosition = question.getPosition();
        int maxPosition = questionRepository.findMaxPositionByTestId(test.getId()).orElse(0);

        if (newPosition < 1 || newPosition > maxPosition) {
            throw new InvalidPositionException("Position must be between 1 and " + maxPosition);
        }

        if (oldPosition < newPosition) {
            questionRepository.decrementPositionForRange(test.getId(), oldPosition + 1, newPosition);
        } else if (oldPosition > newPosition) {
            questionRepository.incrementPositionForRange(test.getId(), newPosition, oldPosition - 1);
        }

        question.setPosition(newPosition);
        questionRepository.save(question);
    }

    private void validateQuestionData(QuestionType questionType, String questionText,
                                      Object image, List<OptionDTO> options) {
        if (questionType == null) {
            throw new IllegalQuestionTypeException("Question type must be provided");
        }

        boolean hasText = questionText != null && !questionText.trim().isEmpty();
        boolean hasImage = image != null;
        boolean hasOptions = options != null && !options.isEmpty();

        switch (questionType) {
            case TEXT_ONLY:
                if (!hasText) {
                    throw new InvalidQuestionTextException("TEXT_ONLY question type requires non-empty text");
                }
                if (hasImage) {
                    throw new InvalidImageException("TEXT_ONLY question type cannot have an image");
                }
                break;

            case IMAGE_ONLY:
                if (!hasImage) {
                    throw new InvalidImageException("IMAGE_ONLY question type requires an image");
                }
                if (hasText) {
                    throw new InvalidQuestionTextException("IMAGE_ONLY question type should not have text");
                }
                break;

            case TEXT_WITH_IMAGE:
                if (!hasText) {
                    throw new InvalidQuestionTextException("TEXT_WITH_IMAGE question type requires non-empty text");
                }
                if (!hasImage) {
                    throw new InvalidImageException("TEXT_WITH_IMAGE question type requires an image");
                }
                break;

            case MULTIPLE_CHOICE:
                if (!hasText) {
                    throw new InvalidQuestionTextException("MULTIPLE_CHOICE question type requires non-empty text");
                }
                if (!hasOptions) {
                    throw new InvalidOptionSelectionException("MULTIPLE_CHOICE question type requires at least one option");
                }

                boolean hasCorrectOption = options.stream().anyMatch(OptionDTO::isCorrect);
                if (!hasCorrectOption) {
                    throw new MissingCorrectOptionException("MULTIPLE_CHOICE question must have at least one correct option");
                }
                break;

            case IMAGE_WITH_MULTIPLE_CHOICE:
                if (!hasImage) {
                    throw new InvalidImageException("IMAGE_WITH_MULTIPLE_CHOICE question type requires an image");
                }
                if (!hasOptions) {
                    throw new InvalidOptionSelectionException("IMAGE_WITH_MULTIPLE_CHOICE question type requires at least one option");
                }

                boolean hasCorrectImageOption = options.stream().anyMatch(OptionDTO::isCorrect);
                if (!hasCorrectImageOption) {
                    throw new MissingCorrectOptionException("IMAGE_WITH_MULTIPLE_CHOICE question must have at least one correct option");
                }
                break;

            default:
                throw new IllegalQuestionTypeException("Unsupported question type: " + questionType);
        }
    }

    private String saveImage(MultipartFile image) {
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = image.getOriginalFilename();
            String fileExtension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";

            String filename = UUID.randomUUID() + fileExtension;
            Path filePath = uploadPath.resolve(filename);

            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return filename;
        } catch (IOException e) {
            log.error("Failed to save image", e);
            throw new ImageSaveException("Failed to save image");
        }
    }

    private void deleteImage(String imagePath) {
        try {
            Path path = Paths.get(uploadDir, imagePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("Failed to delete image: {}", e.getMessage());
        }
    }

    private QuestionDetailsDTO convertToQuestionDetailsDTO(Question question) {
        List<OptionDTO> options = question.getOptions() != null
                ? question.getOptions().stream()
                .map(testDTOMapper::convertToOptionDTO)
                .collect(Collectors.toList())
                : Collections.emptyList();

        return QuestionDetailsDTO.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .imagePath(question.getImagePath())
                .score(question.getScore())
                .questionType(question.getQuestionType())
                .options(options)
                .build();
    }
}