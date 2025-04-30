package com.altester.core.serviceImpl.subject;

import com.altester.core.dtos.core_service.subject.SubjectDTO;
import com.altester.core.dtos.core_service.subject.SubjectGroupDTO;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Subject;
import com.altester.core.serviceImpl.group.GroupActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SubjectMapper {

    private final GroupActivityService groupActivityService;

    public SubjectDTO toDto(Subject subject) {
        if (subject == null) {
            return null;
        }

        List<SubjectGroupDTO> groups = subject.getGroups().stream()
                .filter(group -> group.isActive() || groupActivityService.isGroupInFuture(group))
                .map(this::mapGroupToDto)
                .collect(Collectors.toList());

        return new SubjectDTO(
                subject.getId(),
                subject.getName(),
                subject.getShortName(),
                subject.getDescription(),
                subject.getModified(),
                groups
        );
    }

    private SubjectGroupDTO mapGroupToDto(Group group) {
        boolean isInFuture = groupActivityService.isGroupInFuture(group);
        String status = isInFuture ? "Future" : "Active";

        return new SubjectGroupDTO(
                group.getId(),
                group.getName(),
                status
        );
    }

    public List<SubjectDTO> toDtoList(List<Subject> subjects) {
        if (subjects == null) {
            return Collections.emptyList();
        }

        return subjects.stream()
                .map(this::toDto)
                .toList();
    }
}
