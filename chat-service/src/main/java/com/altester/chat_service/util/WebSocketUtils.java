package com.altester.chat_service.util;

import com.altester.chat_service.dto.ChatMessageDTO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebSocketUtils {

  private WebSocketUtils() {}

  public static Map<String, Object> createResponse(String type) {
    Map<String, Object> response = new HashMap<>();
    response.put("type", type);
    return response;
  }

  public static Map<String, Object> createInitialDataResponse(
      List<ChatMessageDTO> unreadMessages,
      Object conversations,
      Map<String, Object> availableUsers) {
    Map<String, Object> response = createResponse("INITIAL_DATA");
    response.put("unreadMessages", unreadMessages);
    response.put("conversations", conversations);
    response.put("availableUsers", availableUsers);
    return response;
  }
}
