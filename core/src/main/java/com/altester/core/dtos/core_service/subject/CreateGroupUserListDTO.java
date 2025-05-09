package com.altester.core.dtos.core_service.subject;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateGroupUserListDTO {
  private long userId;
  private String name;
  private String surname;
  private String username;

  public CreateGroupUserListDTO(
      Long userId, String name, String surname, String username, List<String> subjectNames) {
    this.userId = userId;
    this.name = name;
    this.surname = surname;
    this.username = username;
  }
}
