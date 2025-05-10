package com.altester.chat_service.service;

import java.util.Set;

public interface UserStatusService {

  /**
   * Sets a user's status to online. This method updates the user's status in Redis with an
   * expiration time and broadcasts the status change to other users if the status changed.
   *
   * @param username The username of the user to set online
   */
  void setUserOnline(String username);

  /**
   * Sets a user's status to offline. This method removes the user's online status from Redis and
   * broadcasts the status change to other users.
   *
   * @param username The username of the user to set offline
   */
  void setUserOffline(String username);

  /**
   * Checks if a user is currently online.
   *
   * @param username The username to check
   * @return true if the user is online, false otherwise
   */
  boolean isUserOnline(String username);

  /**
   * Gets a set of all currently online users.
   *
   * @return A set of usernames representing online users
   */
  Set<String> getOnlineUsers();
}
