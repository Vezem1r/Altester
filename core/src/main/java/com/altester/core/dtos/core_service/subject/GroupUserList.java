package com.altester.core.dtos.core_service.subject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupUserList {
  private long userId;
  private String name;
  private String surname;
  private String username;
}
