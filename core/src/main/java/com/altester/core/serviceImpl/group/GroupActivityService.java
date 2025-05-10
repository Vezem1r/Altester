package com.altester.core.serviceImpl.group;

import com.altester.core.config.SemesterConfig;
import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.enums.Semester;
import com.altester.core.repository.GroupRepository;
import java.time.LocalDate;
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

  /**
   * Checks and updates a group's activity status based on current semester settings
   *
   * @param group Group to check and update
   * @return Updated activity status (true if active)
   */
  public boolean checkAndUpdateGroupActivity(Group group) {
    if (group == null) {
      log.warn("Attempted to check activity for null group");
      return false;
    }

    boolean isActiveSemester =
        semesterConfig.isSemesterActive(group.getSemester(), group.getAcademicYear());

    if (isActiveSemester != group.isActive()) {

      log.info(
          "{} group {} as semester {} {} {} active",
          isActiveSemester ? "Active" : "Non active",
          group.getName(),
          group.getSemester(),
          group.getAcademicYear(),
          isActiveSemester ? "is now" : "is no longer");

      group.setActive(isActiveSemester);
      groupRepository.save(group);
    }

    return group.isActive();
  }

  /** Determines if a group belongs to a future semester relative to current date */
  public boolean isGroupInFuture(Group group) {
    if (group == null) {
      return false;
    }

    Semester currentSemester = semesterConfig.getCurrentSemester();
    int currentAcademicYear = semesterConfig.getCurrentAcademicYear();

    if (group.getAcademicYear() > currentAcademicYear) {
      return true;
    }

    if (group.getAcademicYear() == currentAcademicYear) {
      if (currentSemester == Semester.WINTER && group.getSemester() == Semester.SUMMER) {
        return true;
      }

      if (currentSemester == group.getSemester()) {
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

  /**
   * Checks if a group can be modified based on its activity status Active groups and future groups
   * can be modified
   */
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
