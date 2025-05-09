package com.altester.auth.service;

import jakarta.mail.MessagingException;

public interface EmailService {

  /**
   * Sends an email to the specified recipient. Creates and sends a MIME message with the provided
   * subject and HTML content.
   *
   * @param to Email address of the recipient
   * @param subject Subject line of the email
   * @param text HTML content of the email
   * @throws MessagingException if there is an error creating or sending the email
   */
  void sendEmail(String to, String subject, String text) throws MessagingException;
}
