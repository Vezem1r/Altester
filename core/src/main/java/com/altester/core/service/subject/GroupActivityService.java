package com.altester.core.service.subject;

import com.altester.core.config.SemesterConfig;
import com.altester.core.model.subject.Group;
import com.altester.core.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupActivityService {

    private final GroupRepository groupRepository;
    private final SemesterConfig semesterConfig;

    public boolean checkAndUpdateGroupActivity(Group group) {
        if (group == null) {
            log.warn("Attempted to check activity for null group");
            return false;
        }

        if (!group.isActive()) {
            log.debug("Group {} is already inactive", group.getName());
            return false;
        }

        boolean isActiveSemester = semesterConfig.isSemesterActive(
                group.getSemester().toString(),
                group.getAcademicYear()
        );

        if (!isActiveSemester && group.isActive()) {
            log.info("Deactivating group {} as semester {} {} has ended",
                    group.getName(), group.getSemester(), group.getAcademicYear());
            group.setActive(false);
            groupRepository.save(group);
            return false;
        }

        return true;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void updateAllGroupStatuses() {
        log.info("Running scheduled group status update");
        groupRepository.findByActiveTrue().forEach(this::checkAndUpdateGroupActivity);
    }
}