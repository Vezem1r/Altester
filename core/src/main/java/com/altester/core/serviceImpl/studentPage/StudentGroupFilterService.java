package com.altester.core.serviceImpl.studentPage;

import com.altester.core.model.subject.Group;
import com.altester.core.model.subject.enums.Semester;
import com.altester.core.serviceImpl.group.GroupActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentGroupFilterService {
    private final GroupActivityService groupActivityService;

    /**
     * Filters groups that are active and not in the future
     */
    public List<Group> filterCurrentGroups(List<Group> groups) {
        List<Group> currentGroups = new ArrayList<>();

        for (Group group : groups) {
            groupActivityService.checkAndUpdateGroupActivity(group);

            if (group.isActive() && !groupActivityService.isGroupInFuture(group)) {
                currentGroups.add(group);
            }
        }

        return currentGroups;
    }

    /**
     * Filters groups that are inactive and not in the future
     */
    public List<Group> filterPastGroups(List<Group> groups) {
        List<Group> pastGroups = new ArrayList<>();

        for (Group group : groups) {
            groupActivityService.checkAndUpdateGroupActivity(group);

            if (!group.isActive() && !groupActivityService.isGroupInFuture(group)) {
                pastGroups.add(group);
            }
        }

        return pastGroups;
    }

    /**
     * Groups the provided groups by academic year and semester
     */
    public Map<String, List<Group>> groupByPeriod(List<Group> groups) {
        Map<String, List<Group>> groupedByPeriod = new HashMap<>();

        for (Group group : groups) {
            String key = group.getAcademicYear() + "-" + group.getSemester().toString();
            groupedByPeriod.computeIfAbsent(key, k -> new ArrayList<>()).add(group);
        }

        return groupedByPeriod;
    }

    /**
     * Filters grouped groups by academic year and/or semester
     */
    public Map<String, List<Group>> filterGroupsByPeriod(Map<String, List<Group>> groupedByPeriod,
                                                         Integer academicYear, Semester semester) {

        if (academicYear == null && semester == null) {
            return groupedByPeriod;
        }

        Map<String, List<Group>> filtered = new HashMap<>();

        for (Map.Entry<String, List<Group>> entry : groupedByPeriod.entrySet()) {
            String[] parts = entry.getKey().split("-");
            Integer year = Integer.parseInt(parts[0]);
            Semester sem = Semester.valueOf(parts[1]);

            boolean yearMatches = academicYear == null || year.equals(academicYear);
            boolean semesterMatches = semester == null || sem.equals(semester);

            if (yearMatches && semesterMatches) {
                filtered.put(entry.getKey(), entry.getValue());
            }
        }

        return filtered;
    }
}