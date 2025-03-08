package com.altester.core.service.subject;

import com.altester.core.dtos.core_service.subject.CreateTestDTO;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Subject;
import com.altester.core.model.subject.Test;
import com.altester.core.repository.GroupRepository;
import com.altester.core.repository.SubjectRepository;
import com.altester.core.repository.TestRepository;
import com.altester.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;

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
                    .score(createTestDTO.getScore() == 0 ? 0 : 100)
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
}
