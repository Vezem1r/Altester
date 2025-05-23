package com.altester.core.util;

import com.altester.core.model.ApiKey.ApiKey;
import com.altester.core.model.subject.enums.NotificationType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ApiErrorClassifier {

  private static final String ACTION = "The key has been automatically deactivated. ";

  private ApiErrorClassifier() {}

  /**
   * Classifies HTTP status code and error message into a specific notification type
   *
   * @param status HTTP status from the AI service
   * @param errorMessage Error message from the response
   * @return Appropriate NotificationType for the error
   */
  public static NotificationType classifyError(HttpStatus status, String errorMessage) {
    return switch (status.value()) {
      case 401 -> NotificationType.API_KEY_INVALID;
      case 403 -> NotificationType.API_KEY_PERMISSION_DENIED;
      case 400, 422 -> NotificationType.API_KEY_BAD_REQUEST;
      case 404 -> NotificationType.API_KEY_NOT_FOUND;
      case 429 -> {
        // Check if it's rate limit or quota issue
        if (errorMessage != null
            && (errorMessage.toLowerCase().contains("quota")
                || errorMessage.toLowerCase().contains("credit")
                || errorMessage.toLowerCase().contains("balance")
                || errorMessage.toLowerCase().contains("exceeded your current quota"))) {
          yield NotificationType.API_KEY_QUOTA_EXCEEDED;
        }
        yield NotificationType.API_KEY_RATE_LIMITED;
      }
      case 402 -> NotificationType.API_KEY_QUOTA_EXCEEDED;
      case 500, 502, 503, 529 -> NotificationType.API_KEY_SERVER_ERROR;
      default -> NotificationType.API_KEY_ERROR;
    };
  }

  /**
   * Returns a notification title for the given notification error type.
   *
   * @param errorType The notification type of the error
   * @return A descriptive title for the error type
   */
  public String getErrorTitle(NotificationType errorType) {
    return switch (errorType) {
      case API_KEY_INVALID -> "Invalid API Key";
      case API_KEY_RATE_LIMITED -> "Rate Limit Exceeded";
      case API_KEY_QUOTA_EXCEEDED -> "Quota/Credits Exhausted";
      case API_KEY_PERMISSION_DENIED -> "Permission Denied";
      case API_KEY_BAD_REQUEST -> "Request Format Error";
      case API_KEY_SERVER_ERROR -> "AI Service Unavailable";
      case API_KEY_NOT_FOUND -> "Resource Not Found";
      default -> "API Key Error";
    };
  }

  /**
   * Builds a detailed error message for API key notification based on the error type. The message
   * includes the API key name, service name, and specific instructions based on the error type. For
   * critical errors, it mentions automatic deactivation.
   *
   * @param apiKey The API key that experienced the error
   * @param errorMessage The original error message from the AI service
   * @param status The HTTP status code of the error response
   * @param errorType The classified error type
   * @return A formatted error message suitable for user notification
   */
  public String buildErrorMessage(
      ApiKey apiKey, String errorMessage, HttpStatus status, NotificationType errorType) {
    String serviceName = apiKey.getAiServiceName().toString();
    String keyName = apiKey.getName();

    return switch (errorType) {
      case API_KEY_INVALID ->
          String.format(
              "Your API key '%s' for %s is invalid or expired. "
                  + ACTION
                  + ".Please update it in the settings.",
              keyName,
              serviceName);
      case API_KEY_RATE_LIMITED ->
          String.format(
              "Rate limit exceeded for API key '%s' (%s). "
                  + ACTION
                  + "Please wait before retrying or upgrade your plan with %s.",
              keyName,
              serviceName,
              serviceName);
      case API_KEY_QUOTA_EXCEEDED ->
          String.format(
              "You've exceeded your quota/credits for API key '%s' (%s). "
                  + ACTION
                  + "Please top up your account or check your billing with %s.",
              keyName,
              serviceName,
              serviceName);
      case API_KEY_PERMISSION_DENIED ->
          String.format(
              "Permission denied for API key '%s' (%s). "
                  + ACTION
                  + "This key may not have access to the requested resource. Please check your %s account permissions.",
              keyName,
              serviceName,
              serviceName);
      case API_KEY_BAD_REQUEST ->
          String.format(
              "Invalid request format when using API key '%s' (%s). "
                  + ACTION
                  + "Please check your API key or else it might be a bug in our system.",
              keyName,
              serviceName);
      case API_KEY_SERVER_ERROR ->
          String.format(
              "%s is experiencing server issues. API key '%s' couldn't be used. "
                  + ACTION
                  + "Error: %s",
              serviceName,
              keyName,
              status);
      case API_KEY_NOT_FOUND ->
          String.format(
              "Resource not found when using API key '%s' (%s). "
                  + ACTION
                  + "The AI model or endpoint might have been deprecated.",
              keyName,
              serviceName);
      default ->
          String.format(
              "Error using API key '%s' for %s (Status: %s)" + ACTION,
              keyName,
              serviceName,
              status);
    };
  }
}
