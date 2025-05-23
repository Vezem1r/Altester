package com.altester.core.serviceImpl.subject;

import com.altester.core.dtos.core_service.subject.SubjectDTO;
import com.altester.core.model.subject.Subject;
import com.altester.core.repository.SubjectRepository;
import com.altester.core.service.SubjectService;
import com.altester.core.util.CacheablePage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubjectServiceImpl implements SubjectService {

  private final SubjectRepository subjectRepository;
  private final SubjectMapper subjectMapper;

  @Override
  @Cacheable(
      value = "subjects",
      key =
          "'page:' + #page +"
              + "':size:' + #size +"
              + "':query:' + (#searchQuery == null ? '' : #searchQuery)")
  public CacheablePage<SubjectDTO> getAllSubjects(int page, int size, String searchQuery) {
    Pageable pageable = PageRequest.of(page, size);

    Page<Subject> subjectsPage;
    if (!StringUtils.hasText(searchQuery)) {
      subjectsPage = subjectRepository.findAll(pageable);
    } else {
      subjectsPage =
          subjectRepository.findByNameContainingIgnoreCaseOrShortNameContainingIgnoreCase(
              searchQuery, searchQuery, pageable);
    }

    List<SubjectDTO> content = subjectMapper.toDtoList(subjectsPage.getContent());

    Page<SubjectDTO> result = new PageImpl<>(content, pageable, subjectsPage.getTotalElements());
    return new CacheablePage<>(result);
  }
}
