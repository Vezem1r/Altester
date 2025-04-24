package com.altester.core.serviceImpl.subject;

import com.altester.core.dtos.core_service.subject.SubjectDTO;
import com.altester.core.dtos.core_service.subject.SubjectGroupDTO;
import com.altester.core.model.subject.Subject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SubjectMapper {

    public SubjectDTO toDto(Subject subject) {
        if (subject == null) {
            return null;
        }

        List<SubjectGroupDTO> groups = subject.getGroups().stream()
                .map(group -> new SubjectGroupDTO(group.getId(), group.getName()))
                .toList();

        return new SubjectDTO(
                subject.getId(),
                subject.getName(),
                subject.getShortName(),
                subject.getDescription(),
                subject.getModified(),
                groups
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
