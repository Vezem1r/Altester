package com.altester.core.service.subject;

import com.altester.core.dtos.core_service.subject.CreateTestDTO;
import com.altester.core.dtos.core_service.subject.TestsListDTO;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Test;
import com.altester.core.repository.GroupRepository;
import com.altester.core.repository.TestRepository;
import com.altester.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestService {

    private final TestRepository testRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    public void createTest(CreateTestDTO createTestDTO, Principal principal) {
        try {
            Group optGroup = groupRepository.findById(createTestDTO.getGroupId()).orElseThrow(() ->{
                log.error("Group with id {} not found", createTestDTO.getGroupId());
                return new RuntimeException("Group with id " + createTestDTO.getGroupId() + " not found");
            });

            User optTeacher = userRepository.findByUsername(principal.getName()).orElseThrow(()-> {
                log.error("Teacher {} not found", principal.getName());
                return new RuntimeException("Teacher " + principal.getName() + " not found");
            });

            if (!optGroup.getTeacher().equals(optTeacher) && !optTeacher.getRole().equals(RolesEnum.ADMIN)) {
                log.error("Teacher {} is not the teacher", optTeacher.getUsername());
                throw new RuntimeException("Group is teacher is another person");
            }

            LocalDateTime startTime;
            if (createTestDTO.getStartTime() == null) {
                startTime = LocalDateTime.now();
            } else {
                startTime = createTestDTO.getStartTime();
            }

            LocalDateTime endTime = createTestDTO.getEndTime() != null ? createTestDTO.getEndTime() :
                    startTime.plusYears(10);

            Test test = Test.builder()
                    .title(createTestDTO.getTitle())
                    .description(createTestDTO.getDescription() != null ? createTestDTO.getDescription() : "")
                    .score(0)
                    .duration(Math.max(createTestDTO.getDuration(), 0))
                    .isOpen(true)
                    .max_attempts(createTestDTO.getMax_attempts() > 0 ? createTestDTO.getMax_attempts() : Integer.MAX_VALUE)
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();

            testRepository.save(test);

            optGroup.getTests().add(test);
            groupRepository.save(optGroup);
        } catch (Exception e) {
            log.error("Service error during test creation", e);
            throw new RuntimeException(e);
        }
    }

    public void updateTest(CreateTestDTO createTestDTO, Principal principal, long testId) {
        Group optGroup = groupRepository.findByTests_id(testId).orElseThrow(() ->{
            log.error("Test with id {} not found in any group", testId);
            return new RuntimeException("Test with id " + testId + " not found in any group");
        });

        Test test = testRepository.findById(testId).orElseThrow(() -> {
            log.error("Test with id {} not found", testId);
            return new RuntimeException("Test with id " + testId + " not found");
        });

        User teacher = userRepository.findByUsername(principal.getName()).orElseThrow(() -> {
            log.error("User {} not found", principal.getName());
            return new RuntimeException("User " + principal.getName() + " not found");
        });

        if (!optGroup.getTeacher().equals(teacher) && !teacher.getRole().equals(RolesEnum.ADMIN)) {
            log.error("Teacher {} is not the teacher", teacher.getUsername());
            throw new RuntimeException("This test is managed by another teacher");
        }
            test.setTitle(createTestDTO.getTitle());

        if (createTestDTO.getDescription() != null) {
            test.setDescription(createTestDTO.getDescription());
        }

        if (createTestDTO.getDuration() > 0) {
            test.setDuration(createTestDTO.getDuration());
        }
        if (createTestDTO.getMax_attempts() > 0) {
            test.setMax_attempts(createTestDTO.getMax_attempts());
        }
        test.setOpen(createTestDTO.isOpen());

        if (createTestDTO.getStartTime() != null) {
            test.setStartTime(createTestDTO.getStartTime());
        }
        if (createTestDTO.getEndTime() != null && createTestDTO.getEndTime().isAfter(LocalDateTime.now())) {
            test.setEndTime(createTestDTO.getEndTime());
        }

        testRepository.save(test);
        log.info("Test with id {} updated successfully", testId);
    }


    public Page<TestsListDTO> getAllTestsByGroup(Principal principal, Pageable pageable, long groupId) {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> {
            log.error("Group with id {} not found", groupId);
            return new RuntimeException("Group with id " + groupId + " not found");
        });

        User teacher = userRepository.findByUsername(principal.getName()).orElseThrow(() -> {
            log.error("Teacher {} not found", principal.getName());
            return new RuntimeException("Teacher " + principal.getName() + " not found");
        });

        if (!group.getTeacher().equals(teacher) && !teacher.getRole().equals(RolesEnum.ADMIN)) {
            log.error("Teacher {} is not the teacher", teacher.getUsername());
            throw new RuntimeException("This test is managed by another teacher");
        }

        List<TestsListDTO> testsDTOList = group.getTests().stream()
                .map(test -> new TestsListDTO(
                        test.getTitle(),
                        test.getDuration(),
                        test.getScore(),
                        test.getMax_attempts(),
                        test.isOpen(),
                        test.getStartTime(),
                        test.getEndTime()
                ))
                .toList();

        return new PageImpl<>(testsDTOList, pageable, testsDTOList.size());
    }

    public void deleteTest(long id, Principal principal) {
        try {
            Group optGroup = groupRepository.findByTests_id(id).orElseThrow(() ->{
                log.error("Test with id {} not found in any group", id);
                return new RuntimeException("Test with id " + id + " not found in any group");
            });

            User optTeacher = userRepository.findByUsername(principal.getName()).orElseThrow(()-> {
                log.error("Teacher {} not found", principal.getName());
                return new RuntimeException("Teacher " + principal.getName() + " not found");
            });

            if (!optGroup.getTeacher().equals(optTeacher) && !optTeacher.getRole().equals(RolesEnum.ADMIN)) {
                log.error("Teacher {} is not the teacher", optTeacher.getUsername());
                throw new RuntimeException("This test is teaching another person");
            }

            Optional<Test> optTest = testRepository.findById(id);
            if (optTest.isEmpty()) {
                log.error("Test with id {} not found", id);
                throw new RuntimeException("Test with id " + id + " not found");
            }

            testRepository.deleteById(id);

        } catch (Exception e) {
            log.error("Service error during test deletion", e);
            throw new RuntimeException(e);
        }
    }
}
