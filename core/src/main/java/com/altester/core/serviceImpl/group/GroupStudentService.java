package com.altester.core.serviceImpl.group;

import com.altester.core.dtos.core_service.subject.*;
import com.altester.core.exception.ResourceNotFoundException;
import com.altester.core.exception.ValidationException;
import com.altester.core.model.auth.User;
import com.altester.core.model.auth.enums.RolesEnum;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Subject;
import com.altester.core.repository.GroupRepository;
import com.altester.core.repository.SubjectRepository;
import com.altester.core.repository.UserRepository;
import com.altester.core.util.CacheablePage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GroupStudentService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final GroupDTOMapper groupMapper;

    public Group getGroupById(long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Group with id: {} not found", id);
                    return ResourceNotFoundException.group(id);
                });
    }

    @Cacheable(value = "groupStudents",
            key = "'page:' + #page + ':size:' + #size + ':search:' + (#searchQuery == null ? '' : #searchQuery)")
    public CacheablePage<CreateGroupUserListDTO> getAllStudents(int page, int size, String searchQuery) {
        Pageable pageable = PageRequest.of(page, size);

        Page<User> studentsPage;

        if (StringUtils.hasText(searchQuery)) {
            String searchLower = searchQuery.toLowerCase();
            List<User> allStudents = userRepository.findAllByRole(RolesEnum.STUDENT);

            List<User> filteredStudents = allStudents.stream()
                    .filter(student -> {
                        String fullName = (student.getName() + " " + student.getSurname()).toLowerCase();
                        String username = student.getUsername() != null ? student.getUsername().toLowerCase() : "";

                        return fullName.contains(searchLower) || username.contains(searchLower);
                    })
                    .collect(Collectors.toList());

            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), filteredStudents.size());

            if (start > filteredStudents.size()) {
                Page<CreateGroupUserListDTO> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, filteredStudents.size());
                return new CacheablePage<>(emptyPage);
            }

            List<User> pagedStudents = filteredStudents.subList(start, end);
            studentsPage = new PageImpl<>(pagedStudents, pageable, filteredStudents.size());
        } else {
            studentsPage = userRepository.findByRole(RolesEnum.STUDENT, pageable);
        }

        List<CreateGroupUserListDTO> students = studentsPage.getContent().stream()
                .map(student -> {
                    List<Group> studentActiveGroups = groupRepository.findByStudentsContainingAndActiveTrue(student);

                    List<String> subjectNames = studentActiveGroups.stream()
                            .map(group -> subjectRepository.findByGroupsContaining(group)
                                    .map(Subject::getShortName)
                                    .orElse("Group has no subject"))
                            .distinct()
                            .toList();

                    return groupMapper.toCreateGroupUserListDTO(student, subjectNames);
                })
                .toList();

        Page<CreateGroupUserListDTO> resultPage = new PageImpl<>(students, pageable, studentsPage.getTotalElements());
        return new CacheablePage<>(resultPage);
    }

    @Cacheable(value = "groupStudentsWithCategories",
            key = "'page:' + #page + ':size:' + #size + ':groupId:' + #groupId + ':search:' + (#searchQuery == null ? '' : #searchQuery) + ':includeMembers:' + #includeCurrentMembers + ':hideStudents:' + #hideStudentsInSameSubject")
    public GroupStudentsResponseDTO getGroupStudentsWithCategories(
            int page, int size, Long groupId, String searchQuery, boolean includeCurrentMembers, boolean hideStudentsInSameSubject) {

        if (groupId == null) {
            throw ValidationException.invalidParameter("groupId", "Group ID is required");
        }

        Group group = getGroupById(groupId);

        List<String> subjectNames = group.getStudents().stream()
                .flatMap(student -> getStudentSubjects(student).stream())
                .distinct()
                .collect(Collectors.toList());

        List<CreateGroupUserListDTO> currentMembers = groupMapper.mapAndSortCurrentMembers(
                group.getStudents(), subjectNames);

        CacheablePage<CreateGroupUserListDTO> availableStudents =
                includeCurrentMembers ?
                        getAllStudents(page, size, searchQuery) :
                        getAllStudentsNotInGroup(page, size, groupId, searchQuery, hideStudentsInSameSubject);

        return GroupStudentsResponseDTO.builder()
                .currentMembers(currentMembers)
                .availableStudents(availableStudents)
                .build();
    }

    @Cacheable(
            value = "groupStudentsNotInGroup",
            key = "'page:' + #page + ':size:' + #size + ':groupId:' + #groupId +" +
                    "':search:' + (#searchQuery == null ? '' : #searchQuery) + ':hide:' + #hideStudentsInSameSubject")
    public CacheablePage<CreateGroupUserListDTO> getAllStudentsNotInGroup(
            int page, int size, Long groupId, String searchQuery, boolean hideStudentsInSameSubject) {
        Pageable pageable = PageRequest.of(page, size);

        Group group = getGroupById(groupId);
        Set<Long> studentsInGroupIds = group.getStudents().stream().map(User::getId).collect(Collectors.toSet());
        Subject subject = subjectRepository.findByGroupsContaining(group).orElse(null);

        Set<Long> studentsInSubjectIds = getOtherGroupsStudentIds(subject, group);

        List<User> allStudents = userRepository.findAllByRole(RolesEnum.STUDENT);

        List<User> filteredStudents = filterStudents(
                allStudents, studentsInGroupIds, studentsInSubjectIds, searchQuery, hideStudentsInSameSubject
        );

        sortBySubjectPresence(filteredStudents, studentsInSubjectIds, hideStudentsInSameSubject);

        List<User> pagedStudents = paginateStudents(filteredStudents, pageable);
        List<CreateGroupUserListDTO> dtoList = convertToDto(pagedStudents, studentsInSubjectIds, subject);

        return new CacheablePage<>(new PageImpl<>(dtoList, pageable, filteredStudents.size()));
    }

    public List<User> filterStudents(
            List<User> allStudents,
            Set<Long> studentsInGroupIds,
            Set<Long> studentsInSubjectIds,
            String searchQuery,
            boolean hideStudentsInSameSubject
    ) {
        return allStudents.stream()
                .filter(student -> !studentsInGroupIds.contains(student.getId()))
                .filter(student -> !hideStudentsInSameSubject || !studentsInSubjectIds.contains(student.getId()))
                .filter(student -> {
                    if (!StringUtils.hasText(searchQuery)) return true;

                    String fullName = (student.getName() + " " + student.getSurname()).toLowerCase();
                    String username = Optional.ofNullable(student.getUsername()).orElse("").toLowerCase();
                    return fullName.contains(searchQuery.toLowerCase()) || username.contains(searchQuery.toLowerCase());
                })
                .collect(Collectors.toList());
    }

    public void sortBySubjectPresence(List<User> students, Set<Long> studentsInSubjectIds, boolean hideStudentsInSameSubject) {
        if (!studentsInSubjectIds.isEmpty() && !hideStudentsInSameSubject) {
            students.sort((a, b) -> {
                boolean aInSubject = studentsInSubjectIds.contains(a.getId());
                boolean bInSubject = studentsInSubjectIds.contains(b.getId());
                return Boolean.compare(bInSubject, aInSubject);
            });
        }
    }

    public List<User> paginateStudents(List<User> students, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), students.size());

        if (start >= students.size()) return Collections.emptyList();
        return students.subList(start, end);
    }

    public List<CreateGroupUserListDTO> convertToDto(
            List<User> students,
            Set<Long> studentsInSubjectIds,
            Subject subject
    ) {
        return students.stream().map(student -> {
            List<Group> activeGroups = groupRepository.findByStudentsContainingAndActiveTrue(student);

            List<String> subjectNames = activeGroups.stream()
                    .map(group -> subjectRepository.findByGroupsContaining(group)
                            .map(Subject::getShortName)
                            .orElse("Group has no subject"))
                    .distinct()
                    .toList();

            CreateGroupUserListDTO dto = groupMapper.toCreateGroupUserListDTO(student, subjectNames);

            if (subject != null && studentsInSubjectIds.contains(student.getId())) {
                groupMapper.enrichWithSubjectInfo(dto, true, subject.getName(), subject.getShortName());
            }

            return dto;
        }).toList();
    }

    public Set<Long> getOtherGroupsStudentIds(Subject subject, Group currentGroup) {
        if (subject == null) return Collections.emptySet();

        return subject.getGroups().stream()
                .filter(g -> g.getId() != currentGroup.getId())
                .flatMap(g -> g.getStudents().stream())
                .map(User::getId)
                .collect(Collectors.toSet());
    }

    public List<String> getStudentSubjects(User student) {
        List<Group> studentActiveGroups = groupRepository.findByStudentsContainingAndActiveTrue(student);

        return studentActiveGroups.stream()
                .map(group -> subjectRepository.findByGroupsContaining(group)
                        .map(Subject::getShortName)
                        .orElse("Group has no subject"))
                .distinct()
                .collect(Collectors.toList());
    }
}