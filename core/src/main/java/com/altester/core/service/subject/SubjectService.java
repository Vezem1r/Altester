package com.altester.core.service.subject;

import com.altester.core.dtos.core_service.subject.CreateSubjectDTO;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.Subject;
import com.altester.core.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public Page<Subject> getAllSubjects(Pageable pageable) {
        try {
            log.info("Getting subjects with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
            return subjectRepository.findAll(pageable);
        } catch (Exception e) {
            log.error("Error fetching subjects: {}", e.getMessage());
            return Page.empty();
        }
    }

    public Subject getSubject(long subjectId) {
        try {
            return subjectRepository.findById(subjectId).orElseThrow(() -> {
                log.error("Subject with id {} not found", subjectId);
                return new RuntimeException("Subject not found");
            });
        } catch (Exception e) {
            log.error("Subject with id {} not found", subjectId);
            throw new RuntimeException(e.getMessage());
        }
    }

    public void updateSubject(CreateSubjectDTO createSubjectDTO, long subjectId) {
        try {
            Subject subject = subjectRepository.findById(subjectId).orElseThrow(() -> {
                log.error("Subject with id {} not found", subjectId);
                return new RuntimeException("Subject not found");
            });

            Optional<Subject> optionalSubject = subjectRepository.findByShortName(createSubjectDTO.getShortName().toUpperCase());
            if (optionalSubject.isPresent() && optionalSubject.get().getId() != subject.getId()) {
                log.error("Subject with short name: {} already exists", createSubjectDTO.getShortName());
                throw new RuntimeException("Subject with short name: " + createSubjectDTO.getShortName() + " already exists");
            }

            subject.setName(createSubjectDTO.getName());
            subject.setShortName(createSubjectDTO.getShortName().toUpperCase());
            subject.setDescription(createSubjectDTO.getDescription());

            subjectRepository.save(subject);

        } catch (Exception e) {
            log.error("Subject with id {} not found", subjectId);
            throw new RuntimeException(e.getMessage());
        }
    }

    public void deleteSubject(long subjectId) {
        try {
            subjectRepository.findById(subjectId).orElseThrow(() -> {
                log.error("Subject with id {} not found", subjectId);
                return new RuntimeException("Subject with id " + subjectId + " not found");
            });
            subjectRepository.deleteById(subjectId);
        } catch (Exception e) {
            log.error("Subject with id {} not found", subjectId);
            throw new RuntimeException(e.getMessage());
        }
    }

    public void createSubject(CreateSubjectDTO createSubjectDTO) {
        try {
            Optional<Subject> optSubject = subjectRepository.findByShortName(createSubjectDTO.getShortName().toUpperCase());
            if (optSubject.isPresent()) {
                log.error("Subject with short name {} already exists", createSubjectDTO.getShortName());
                throw new RuntimeException("Subject with short name already exists");
            }

            Subject subject = Subject.builder()
                    .name(createSubjectDTO.getName())
                    .shortName(createSubjectDTO.getShortName().toUpperCase())
                    .description(createSubjectDTO.getDescription())
                    .build();

            subjectRepository.save(subject);
            log.info("Subject with short name {} created", createSubjectDTO.getShortName());
        } catch (Exception e) {
            log.error("Error creating subject {}", createSubjectDTO.getShortName());
            throw new RuntimeException(e.getMessage());
        }
    }

    public void updateGroups(long subjectId, Set<Group> groups) {
        try{
            Subject subject = subjectRepository.findById(subjectId).orElseThrow(() -> {
                log.error("Subject with id {} not found", subjectId);
                return new RuntimeException("Subject with id " + subjectId + " not found");
            });

            Set<Group> currentGroups = subject.getGroups();

            for (Group group : groups) {
                if (!currentGroups.contains(group)) {
                    currentGroups.add(group);
                }
            }

            currentGroups.removeIf(group -> !groups.contains(group));

            subject.setGroups(currentGroups);
            subjectRepository.save(subject);
        } catch (Exception e) {
            log.error("Error updating groups {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
}
