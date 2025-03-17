package com.altester.core.controller.AdminPage;

import com.altester.core.dtos.core_service.subject.*;
import com.altester.core.exception.GroupInactiveException;
import com.altester.core.service.subject.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/group")
@Slf4j
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @GetMapping("/getGroupStudents")
    public ResponseEntity<Page<CreateGroupUserListDTO>> getGroupStudents(@RequestParam(defaultValue = "0") int page) {
        try {
            int fixedSize = 10;
            Pageable pageable = PageRequest.of(page, fixedSize);
            Page<CreateGroupUserListDTO> students = groupService.getAllStudents(pageable);
            log.info("Students fetched successfully");
            return ResponseEntity.status(HttpStatus.OK).body(students);
        } catch (Exception e) {
            log.error("Students fetch failed");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/getGroupTeachers")
    public ResponseEntity<Page<GroupUserList>> getGroupTeachers(@RequestParam(defaultValue = "0") int page) {
        try {
            int fixedSize = 10;
            Pageable pageable = PageRequest.of(page, fixedSize);
            Page<GroupUserList> students = groupService.getAllTeachers(pageable);
            log.info("Teachers fetched successfully");
            return ResponseEntity.status(HttpStatus.OK).body(students);
        } catch (Exception e) {
            log.error("Teachers fetch failed");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Long> createGroup(@RequestBody CreateGroupDTO createGroupDTO) {
        try {
            Long id = groupService.createGroup(createGroupDTO);
            log.info("Group {} created successfully with ID {}", createGroupDTO.getGroupName(), id);
            return ResponseEntity.status(HttpStatus.CREATED).body(id);
        } catch (GroupInactiveException e) {
            log.error("Cannot create group: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            log.error("Group {} creation failed: {}", createGroupDTO.getGroupName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateGroup(@RequestBody CreateGroupDTO createGroupDTO,
                                              @PathVariable long id) {
        try {
            groupService.updateGroup(id, createGroupDTO);
            return ResponseEntity.status(HttpStatus.OK).body("Group updated successfully");
        } catch (Exception e) {
            log.error("Group {} update failed", createGroupDTO.getGroupName());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error during group updating. " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<Page<GroupsResponce>> getAllGroups(@RequestParam(defaultValue = "0") int page) {
        try {
            int fixedSize = 10;
            Pageable pageable = PageRequest.of(page, fixedSize);
            Page<GroupsResponce> groups = groupService.getAllGroups(pageable);
            log.info("Groups fetched successfully");
            return ResponseEntity.status(HttpStatus.OK).body(groups);
        } catch (Exception e) {
            log.error("Groups fetch failed");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupDTO> getGroup(@PathVariable long id) {
        try {
            GroupDTO group = groupService.getGroup(id);
            log.info("Group with id {} fetched successfully", id);
            return ResponseEntity.status(HttpStatus.OK).body(group);
        } catch (Exception e) {
            log.error("Group {} get failed", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteGroup(@PathVariable long id) {
        try {
            groupService.deleteGroup(id);
            return ResponseEntity.status(HttpStatus.OK).body("Group deleted successfully");
        } catch (Exception e) {
            log.error("Group {} delete failed", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error during group deleting. " + e.getMessage());
        }
    }
}