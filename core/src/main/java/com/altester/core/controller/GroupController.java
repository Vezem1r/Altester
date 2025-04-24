package com.altester.core.controller;

import com.altester.core.dtos.core_service.subject.*;
import com.altester.core.service.GroupService;
import com.altester.core.util.CacheablePage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/group")
@Slf4j
@RequiredArgsConstructor
@Validated
public class GroupController {

    private final GroupService groupService;

    @PostMapping("/create")
    public ResponseEntity<Long> createGroup(@Valid @RequestBody CreateGroupDTO createGroupDTO) {
        log.debug("Creating new group: {}", createGroupDTO.getGroupName());
        Long groupId = groupService.createGroup(createGroupDTO);
        log.info("Group created successfully with ID: {}", groupId);
        return ResponseEntity.status(HttpStatus.CREATED).body(groupId);
    }

    @PutMapping("/update/{groupId}")
    public ResponseEntity<String> updateGroup(
            @PathVariable Long groupId,
            @Valid @RequestBody CreateGroupDTO createGroupDTO) {
        log.debug("Updating group with ID {}: {}", groupId, createGroupDTO.getGroupName());
        groupService.updateGroup(groupId, createGroupDTO);
        log.info("Group with ID {} updated successfully", groupId);
        return ResponseEntity.ok("Group updated successfully");
    }

    @DeleteMapping("/delete/{groupId}")
    public ResponseEntity<String> deleteGroup(
            @PathVariable Long groupId) {
        log.debug("Deleting group with ID: {}", groupId);
        groupService.deleteGroup(groupId);
        log.info("Group with ID {} deleted successfully", groupId);
        return ResponseEntity.ok("Group deleted successfully");
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDTO> getGroup(
            @PathVariable Long groupId) {
        log.debug("Fetching group with ID: {}", groupId);
        GroupDTO group = groupService.getGroup(groupId);
        log.debug("Group with ID {} fetched successfully", groupId);
        return ResponseEntity.ok(group);
    }

    @GetMapping("/all")
    public ResponseEntity<Page<GroupsResponse>> getAllGroups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) String activityFilter,
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) Long subjectId) {
        log.debug("Fetching groups with page={}, size={}, searchQuery={}, activityFilter={}, available={}, subjectId={}",
                page, size, searchQuery, activityFilter, available, subjectId);

        CacheablePage<GroupsResponse> groups = groupService.getAllGroups(page, size, searchQuery, activityFilter, available, subjectId);

        log.debug("Retrieved {} groups", groups.getTotalElements());
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/getGroupTeachers")
    public ResponseEntity<Page<GroupUserList>> getAllTeachers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchQuery) {
        log.debug("Fetching all teachers with page={}, size={}, searchQuery={}", page, size, searchQuery);

        CacheablePage<GroupUserList> teachers = groupService.getAllTeachers(page, size, searchQuery);

        log.debug("Retrieved {} teachers", teachers.getTotalElements());
        return ResponseEntity.ok(teachers);
    }

    @GetMapping("/getGroupStudents")
    public ResponseEntity<GroupStudentsResponseDTO> getGroupStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(defaultValue = "false") boolean includeCurrentMembers,
            @RequestParam(defaultValue = "false") boolean hideStudentsInSameSubject) {
        log.debug("Fetching students for group with ID: {}", groupId);

        if (groupId == null) {
            log.info("Request for students without groupId. Returning all students.");
            CacheablePage<CreateGroupUserListDTO> allStudents = groupService.getAllStudents(page, size, searchQuery);

            GroupStudentsResponseDTO result = GroupStudentsResponseDTO.builder()
                    .currentMembers(List.of())
                    .availableStudents(allStudents)
                    .build();

            log.debug("Retrieved {} current students", result.getCurrentMembers().size());

            return ResponseEntity.ok(result);
        }

        GroupStudentsResponseDTO result = groupService.getGroupStudentsWithCategories(
                page, size, groupId, searchQuery, includeCurrentMembers, hideStudentsInSameSubject);

        return ResponseEntity.ok(result);
    }
}