package com.altester.core.dtos.core_service.TeacherPage;

import com.altester.core.dtos.core_service.subject.GroupUserList;
import com.altester.core.model.subject.enums.Semester;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeacherGroupDetailDTO {
  private long id;
  private String name;
  private String subject;
  private List<GroupUserList> students;
  private Semester semester;
  private Integer academicYear;
  private boolean active;
  private boolean isInFuture;
  private List<TeacherOtherGroupDTO> otherTeacherGroups;
}
