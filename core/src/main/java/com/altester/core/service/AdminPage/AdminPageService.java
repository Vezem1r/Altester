package com.altester.core.service.AdminPage;

import com.altester.core.dtos.AdminPage.AdminPageDTO;
import com.altester.core.dtos.AdminPage.UsersListDTO;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.repository.GroupRepository;
import com.altester.core.repository.SubjectRepository;
import com.altester.core.repository.TestRepository;
import com.altester.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminPageService {
    private final UserRepository userRepository;
    private final TestRepository testRepository;
    private final GroupRepository groupRepository;
    private final SubjectRepository subjectRepository;

    public Page<UsersListDTO> getStudents(int page) {
        try {
            return userRepository.findByRole(RolesEnum.STUDENT, PageRequest.of(page, 20))
                    .map(this::convertToUsersListDTO);
        } catch (Exception e) {
            log.error("Error fetching students: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch students", e);
        }
    }

    public Page<UsersListDTO> getTeachers(int page) {
        try {
            return userRepository.findByRole(RolesEnum.TEACHER, PageRequest.of(page, 20))
                    .map(this::convertToUsersListDTO);
        } catch (Exception e) {
            log.error("Error fetching teachers: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch teachers", e);
        }
    }

    private UsersListDTO convertToUsersListDTO(User user) {
        UsersListDTO dto = new UsersListDTO();
        dto.setFirstName(user.getName());
        dto.setLastName(user.getSurname());
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername());
        dto.setLastLogin(user.getLastLogin());
        return dto;
    }

    public AdminPageDTO getPage() {
        try {
            AdminPageDTO dto = new AdminPageDTO();
            dto.setStudentsCount(userRepository.countByRole(RolesEnum.STUDENT));
            dto.setTeachersCount(userRepository.countByRole(RolesEnum.TEACHER));
            dto.setGroupsCount(groupRepository.count());
            dto.setSubjectsCount(subjectRepository.count());
            dto.setTestsCount(testRepository.count());
            return dto;
        } catch (Exception e) {
            log.error("Error fetching admin page stats: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch admin page stats", e);
        }
    }
}
