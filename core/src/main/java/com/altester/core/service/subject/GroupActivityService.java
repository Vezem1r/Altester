package com.altester.core.service.subject;

import com.altester.core.config.SemesterConfig;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.enums.Semester;
import com.altester.core.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

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

        boolean isActiveSemester = semesterConfig.isSemesterActive(
                group.getSemester().toString(),
                group.getAcademicYear()
        );

        if (isActiveSemester != group.isActive()) {
            log.info("{} group {} as semester {} {} {} active",
                    isActiveSemester ? "Activating" : "Deactivating",
                    group.getName(),
                    group.getSemester(),
                    group.getAcademicYear(),
                    isActiveSemester ? "is now" : "is no longer");

            group.setActive(isActiveSemester);
            groupRepository.save(group);
        }

        return group.isActive();
    }

    public boolean isGroupInFuture(Group group) {
        if (group == null) {
            return false;
        }

        String currentSemester = semesterConfig.getCurrentSemester();
        int currentAcademicYear = semesterConfig.getCurrentAcademicYear();

        if (group.getAcademicYear() > currentAcademicYear) {
            return true;
        }

        if (group.getAcademicYear() == currentAcademicYear) {
            if (currentSemester.equals("WINTER") && group.getSemester() == Semester.SUMMER) {
                return true;
            }

            if (currentSemester.equals(group.getSemester().toString())) {
                LocalDate now = LocalDate.now();

                if (group.getSemester() == Semester.WINTER) {
                    LocalDate winterStart = LocalDate.of(currentAcademicYear, 9, 1);
                    return now.isBefore(winterStart);
                } else if (group.getSemester() == Semester.SUMMER) {
                    LocalDate summerStart = LocalDate.of(currentAcademicYear + 1, 2, 1);
                    return now.isBefore(summerStart);
                }
            }
        }

        return false;
    }

    public boolean canModifyGroup(Group group) {
        if (group == null) {
            return false;
        }

        checkAndUpdateGroupActivity(group);

        if (group.isActive()) {
            return true;
        }

        return isGroupInFuture(group);
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void updateAllGroupStatuses() {
        log.info("Running scheduled group status update");
        groupRepository.findAll().forEach(this::checkAndUpdateGroupActivity);
    }
}