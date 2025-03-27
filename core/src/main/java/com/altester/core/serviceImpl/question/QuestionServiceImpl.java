package com.altester.core.serviceImpl.question;

import com.altester.core.dtos.core_service.question.CreateQuestionDTO;
import com.altester.core.dtos.core_service.question.QuestionDetailsDTO;
import com.altester.core.dtos.core_service.question.UpdateQuestionDTO;
import com.altester.core.exception.*;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Option;
import com.altester.core.model.subject.Question;
import com.altester.core.model.subject.Test;
import com.altester.core.repository.*;
import com.altester.core.service.QuestionService;
import com.altester.core.serviceImpl.test.TestAccessValidator;
import com.altester.core.serviceImpl.test.TestDTOMapper;
import com.altester.core.serviceImpl.group.GroupActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {
    private final QuestionRepository questionRepository;
    private final TestRepository testRepository;
    private final UserRepository userRepository;
    private final OptionRepository optionRepository;
    private final GroupRepository groupRepository;
    private final TestAccessValidator testAccessValidator;
    private final TestDTOMapper testDTOMapper;
    private final QuestionDTOMapper questionDTOMapper;
    private final GroupActivityService groupActivityService;
    private final ImageService imageService;
    private final QuestionValidator questionValidator;

    private User getCurrentUser(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> {
                    log.error("User {} not found", principal.getName());
                    return ResourceNotFoundException.user(principal.getName());
                });
    }

    private Test getTestById(Long testId) {
        return testRepository.findById(testId)
                .orElseThrow(() -> {
                    log.error("Test with ID {} not found", testId);
                    return ResourceNotFoundException.test(testId);
                });
    }

    private Question getQuestionById(Long questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> {
                    log.error("Question with ID {} not found", questionId);
                    return ResourceNotFoundException.question(questionId);
                });
    }

    @Override
    @Transactional
    public QuestionDetailsDTO addQuestion(Long testId, CreateQuestionDTO createQuestionDTO,
                                          Principal principal, MultipartFile image) {
        log.info("User {} is attempting to add a question to test with ID {}", principal.getName(), testId);

        User currentUser = getCurrentUser(principal);
        Test test = getTestById(testId);

        verifyTestModificationPermission(currentUser, test);

        questionValidator.validateQuestionData(
                createQuestionDTO.getQuestionType(),
                createQuestionDTO.getQuestionText(),
                image != null && !image.isEmpty(),
                createQuestionDTO.getOptions()
        );

        String imagePath = null;
        if (image != null && !image.isEmpty()) {
            imagePath = imageService.saveImage(image);
            log.info("Image saved successfully: {}", imagePath);
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
        return questionDTOMapper.convertToQuestionDetailsDTO(savedQuestion);
    }

    @Override
    @Transactional
    public QuestionDetailsDTO updateQuestion(Long questionId, UpdateQuestionDTO updateQuestionDTO,
                                             Principal principal, MultipartFile image) {
        log.info("User {} is attempting to update question with ID {}", principal.getName(), questionId);

        User currentUser = getCurrentUser(principal);
        Question question = getQuestionById(questionId);
        Test test = question.getTest();

        verifyTestModificationPermission(currentUser, test);

        String imagePath = question.getImagePath();
        boolean imageChanged = false;

        if (image != null && !image.isEmpty()) {
            if (imagePath != null) {
                imageService.deleteImage(imagePath);
            }
            imagePath = imageService.saveImage(image);
            imageChanged = true;
        } else if (updateQuestionDTO.isRemoveImage() && imagePath != null) {
            imageService.deleteImage(imagePath);
            imagePath = null;
            imageChanged = true;
        }

        String questionText = updateQuestionDTO.getQuestionText() != null ?
                updateQuestionDTO.getQuestionText() : question.getQuestionText();

        boolean hasImage = imagePath != null || imageChanged;
        questionValidator.validateQuestionData(
                updateQuestionDTO.getQuestionType(),
                questionText,
                hasImage,
                updateQuestionDTO.getOptions()
        );

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

        return questionDTOMapper.convertToQuestionDetailsDTO(updatedQuestion);
    }

    @Override
    @Transactional
    public void deleteQuestion(Long questionId, Principal principal) {
        log.info("User {} is attempting to delete question with ID {}", principal.getName(), questionId);

        User currentUser = getCurrentUser(principal);
        Question question = getQuestionById(questionId);
        int position = question.getPosition();
        Test test = question.getTest();

        verifyTestModificationPermission(currentUser, test);

        if (question.getImagePath() != null) {
            imageService.deleteImage(question.getImagePath());
        }

        questionRepository.delete(question);

        questionRepository.decrementPositionForRange(
                test.getId(),
                position + 1,
                Integer.MAX_VALUE
        );

        log.info("Question with ID {} deleted", questionId);
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionDetailsDTO getQuestion(Long questionId, Principal principal) {
        log.info("User {} is attempting to get question with ID {}", principal.getName(), questionId);

        User currentUser = getCurrentUser(principal);
        Question question = getQuestionById(questionId);
        Test test = question.getTest();

        testAccessValidator.validateTestAccess(currentUser, test);

        return questionDTOMapper.convertToQuestionDetailsDTO(question);
    }

    @Override
    @Transactional
    public void changeQuestionPosition(Long questionId, int newPosition, Principal principal) {
        log.info("User {} is attempting to change position of question ID {} to position {}",
                principal.getName(), questionId, newPosition);

        User currentUser = getCurrentUser(principal);
        Question question = getQuestionById(questionId);
        Test test = question.getTest();

        verifyTestModificationPermission(currentUser, test);

        int oldPosition = question.getPosition();
        int maxPosition = questionRepository.findMaxPositionByTestId(test.getId()).orElse(0);

        if (newPosition < 1 || newPosition > maxPosition) {
            throw ValidationException.invalidPosition("Position must be between 1 and " + maxPosition);
        }

        if (oldPosition == newPosition) {
            log.debug("No position change required as old and new positions are the same");
            return;
        }

        if (oldPosition < newPosition) {
            questionRepository.decrementPositionForRange(test.getId(), oldPosition + 1, newPosition);
        } else {
            questionRepository.incrementPositionForRange(test.getId(), newPosition, oldPosition - 1);
        }

        question.setPosition(newPosition);
        questionRepository.save(question);

        log.info("Question ID {} position changed from {} to {}", questionId, oldPosition, newPosition);
    }

    /**
     * Verifies that the current user has permission to modify the test.
     * For teachers, checks if they can edit the specific test.
     * Also verifies that all groups associated with the test are active.
     *
     * @throws AccessDeniedException If the user does not have permission to modify the test
     * @throws StateConflictException If trying to modify a test in a past semester group
     */
    private void verifyTestModificationPermission(User currentUser, Test test) {
        if (currentUser.getRole() == RolesEnum.TEACHER) {
            List<Group> teacherGroups = groupRepository.findByTeacher(currentUser);
            testAccessValidator.validateTeacherEditAccess(currentUser, test, teacherGroups);
        } else if (currentUser.getRole() != RolesEnum.ADMIN) {
            log.warn("User {} with role {} attempted to modify test", currentUser.getUsername(), currentUser.getRole());
            throw AccessDeniedException.testEdit();
        }

        List<Group> testGroups = testDTOMapper.findGroupsByTest(test);
        for (Group group : testGroups) {
            if (!groupActivityService.canModifyGroup(group)) {
                log.warn("Attempt to modify test in past semester group");
                throw StateConflictException.inactiveGroup(group.getName());
            }
        }
    }
}