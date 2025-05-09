package com.altester.chat_service.model.enums;

public enum RolesEnum {
  TEACHER,
  STUDENT;

  public String getRole() {
    return "ROLE_" + this.name();
  }
}
