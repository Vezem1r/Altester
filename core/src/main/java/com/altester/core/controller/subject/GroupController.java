package com.altester.core.controller.subject;

import com.altester.core.dtos.core_service.subject.CreateGroupDTO;
import com.altester.core.dtos.core_service.subject.GroupsResponce;
import com.altester.core.model.subject.Group;
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
@RequestMapping("/teacher/group")
@Slf4j
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping("/create")
    public ResponseEntity<String> createGroup(@RequestBody CreateGroupDTO createGroupDTO) {
        try {
            groupService.createGroup(createGroupDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body("Group created successfully");
        } catch (Exception e) {
            log.error("Group {} creation failed", createGroupDTO.getGroupName());
            throw new RuntimeException("Group creation failed. " + e.getMessage());
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
            throw new RuntimeException("Group {} update failed. " + e.getMessage());
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
            throw new RuntimeException("Groups fetch failed. " + e.getMessage());
        }

    }

    @GetMapping("/{id}")
    public ResponseEntity<Group> getGroup(@PathVariable long id) {
        try {
            Group group = groupService.getGroup(id);
            log.info("Group with id {} fetched successfully", id);
            return ResponseEntity.status(HttpStatus.OK).body(group);
        } catch (Exception e) {
            log.error("Group {} get failed", id);
            throw new RuntimeException("Group get failed. " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteGroup(@PathVariable long id) {
        try {
            groupService.deleteGroup(id);
            return ResponseEntity.status(HttpStatus.OK).body("Group deleted successfully");
        } catch (Exception e) {
            log.error("Group {} delete failed", id);
            throw new RuntimeException("Group get failed. " + e.getMessage());
        }
    }
}
