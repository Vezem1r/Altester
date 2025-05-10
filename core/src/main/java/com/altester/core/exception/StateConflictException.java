package com.altester.core.exception;

import lombok.Getter;

@Getter
public class StateConflictException extends AlTesterException {
  private final String resource;
  private final String currentState;
  private static final String KEY_GROUP = "group";

  public StateConflictException(String resource, String currentState, String message) {
    super(message, ErrorCode.STATE_CONFLICT);
    this.resource = resource;
    this.currentState = currentState;
  }

  public static StateConflictException inactiveGroup(String groupName) {
    return new StateConflictException(
        KEY_GROUP, "inactive", "Cannot perform operation on inactive group: " + groupName);
  }

  public static StateConflictException roleConflict(String message) {
    return new StateConflictException("user role", "incorrect", message);
  }

  public static StateConflictException multipleActiveGroups(String message) {
    return new StateConflictException("groups", "multiple_active", message);
  }

  public static StateConflictException groupAlreadyAssigned(String groupName) {
    return new StateConflictException(
        KEY_GROUP,
        "already_assigned",
        "Group " + groupName + " is already assigned to another subject");
  }

  public static StateConflictException differentSemesters(String fromGroup, String toGroup) {
    return new StateConflictException(
        KEY_GROUP,
        "different_semester",
        "Groups " + fromGroup + " and " + toGroup + " are not in the same semester");
  }
}
