package com.altester.core.serviceImpl.subject;

import com.altester.core.dtos.core_service.subject.CreateSubjectDTO;
import com.altester.core.dtos.core_service.subject.SubjectDTO;
import com.altester.core.dtos.core_service.subject.UpdateGroupsDTO;
import com.altester.core.exception.ResourceAlreadyExistsException;
import com.altester.core.exception.ResourceNotFoundException;
import com.altester.core.model.subject.Subject;
import com.altester.core.repository.SubjectRepository;
import com.altester.core.service.SubjectService;
import com.altester.core.serviceImpl.CacheService;
import com.altester.core.util.CacheablePage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final SubjectGroupService subjectGroupService;
    private final CacheService cacheService;
    private final SubjectMapper subjectMapper;

    @Override
    @Cacheable(value = "subjects",
            key = "'page:' + #page +" + "':size:' + #size +" + "':query:' + (#searchQuery == null ? '' : #searchQuery)")
    public CacheablePage<SubjectDTO> getAllSubjects(int page, int size, String searchQuery) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Subject> subjectsPage;
        if (!StringUtils.hasText(searchQuery)) {
            subjectsPage = subjectRepository.findAll(pageable);
        } else {
            subjectsPage = subjectRepository
                    .findByNameContainingIgnoreCaseOrShortNameContainingIgnoreCase(
                            searchQuery, searchQuery, pageable);
        }

        List<SubjectDTO> content = subjectMapper.toDtoList(subjectsPage.getContent());

        Page<SubjectDTO> result = new PageImpl<>(content, pageable, subjectsPage.getTotalElements());
        return new CacheablePage<>(result);
    }

    @Override
    @Transactional
    public void updateSubject(CreateSubjectDTO createSubjectDTO, long subjectId) {
        if (createSubjectDTO == null) {
            throw new IllegalArgumentException("Subject data cannot be null");
        }

        Subject subject = subjectRepository.findById(subjectId).orElseThrow(() -> {
            log.error("Subject with id {} not found", subjectId);
            return ResourceNotFoundException.subject(subjectId);
        });

        String shortName = createSubjectDTO.getShortName().toUpperCase();
        Optional<Subject> optionalSubject = subjectRepository.findByShortName(shortName);
        if (optionalSubject.isPresent() && optionalSubject.get().getId() != subject.getId()) {
            log.error("Subject with short name: {} already exists", shortName);
            throw ResourceAlreadyExistsException.subject(shortName);
        }

        subject.setName(createSubjectDTO.getName());
        subject.setShortName(shortName);
        subject.setDescription(createSubjectDTO.getDescription());
        subject.setModified(LocalDateTime.now());

        subjectRepository.save(subject);

        cacheService.clearSubjectRelatedCaches();

        log.info("Subject with id {} updated successfully", subjectId);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"subjects", "adminStats"}, allEntries = true)
    public void deleteSubject(long subjectId) {
        Subject subject = subjectRepository.findById(subjectId).orElseThrow(() -> {
            log.error("Subject with id {} not found", subjectId);
            return ResourceNotFoundException.subject(subjectId);
        });

        try {
            subjectRepository.delete(subject);
            cacheService.clearAllCaches();
            log.info("Subject with id {} deleted successfully", subjectId);
        } catch (Exception e) {
            log.error("Error during subject delete: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete subject: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"subjects", "adminStats"}, allEntries = true)
    public void createSubject(CreateSubjectDTO createSubjectDTO) {
        if (createSubjectDTO == null) {
            throw new IllegalArgumentException("Subject data cannot be null");
        }

        String shortName = createSubjectDTO.getShortName().toUpperCase();
        Optional<Subject> optSubject = subjectRepository.findByShortName(shortName);
        if (optSubject.isPresent()) {
            log.error("Subject with short name {} already exists", shortName);
            throw ResourceAlreadyExistsException.subject(shortName);
        }

        Subject subject = Subject.builder()
                .name(createSubjectDTO.getName())
                .shortName(shortName)
                .description(createSubjectDTO.getDescription())
                .modified(LocalDateTime.now())
                .build();

        subjectRepository.save(subject);
        cacheService.clearAllCaches();
        log.info("Subject with short name {} created", shortName);
    }

    @Override
    @Transactional
    public void updateGroups(UpdateGroupsDTO updateGroupsDTO) {
        subjectGroupService.updateGroups(updateGroupsDTO);
    }

    @Override
    @Transactional
    public void updateGroup(long subjectId, long groupId) {
        subjectGroupService.updateGroup(subjectId, groupId);
    }
}