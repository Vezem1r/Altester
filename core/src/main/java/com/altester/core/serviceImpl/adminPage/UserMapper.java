package com.altester.core.serviceImpl.adminPage;

import com.altester.core.dtos.core_service.AdminPage.UsersListDTO;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Subject;
import com.altester.core.repository.GroupRepository;
import com.altester.core.repository.SubjectRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

  private final GroupRepository groupRepository;
  private final SubjectRepository subjectRepository;

  public UsersListDTO convertToUsersListDTO(User user) {
    UsersListDTO dto = new UsersListDTO();
    dto.setFirstName(user.getName());
    dto.setLastName(user.getSurname());
    dto.setEmail(user.getEmail());
    dto.setUsername(user.getUsername());
    dto.setLastLogin(user.getLastLogin());
    dto.setRegistered(user.isRegistered());

    List<Group> userGroups;
    if (user.getRole() == RolesEnum.STUDENT) {
      userGroups = groupRepository.findAllByStudentsContaining(user);
    } else if (user.getRole() == RolesEnum.TEACHER) {
      userGroups = groupRepository.findAllByTeacher(user);
    } else {
      userGroups = List.of();
    }

    List<Group> activeUserGroups = userGroups.stream().filter(Group::isActive).toList();

    dto.setGroupNames(activeUserGroups.stream().map(Group::getName).toList());

    List<String> subjectNames =
        activeUserGroups.stream()
            .map(group -> subjectRepository.findByGroupsId(group.getId()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(Subject::getShortName)
            .distinct()
            .toList();

    dto.setSubjectShortNames(subjectNames);

    return dto;
  }
}
