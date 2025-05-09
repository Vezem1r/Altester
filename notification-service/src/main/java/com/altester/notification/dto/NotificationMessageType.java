package com.altester.notification.dto;

public enum NotificationMessageType {
  INITIAL_DATA,
  NEW_NOTIFICATION,
  UNREAD_COUNT;

  @Override
  public String toString() {
    return name();
  }
}
