package com.altester.core.util;

import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileValidator {

  private static final List<String> ALLOWED_IMAGE_TYPES =
      Arrays.asList("image/jpeg", "image/png", "image/gif");

  private static final long DEFAULT_MAX_FILE_SIZE = 10 * 1024 * 1024;

  public void validateImage(MultipartFile file, long maxSizeBytes) {
    if (file == null || file.isEmpty()) {
      return;
    }

    if (file.getSize() > maxSizeBytes) {
      throw new IllegalArgumentException(
          String.format("File size exceeds the maximum allowed (%d bytes)", maxSizeBytes));
    }

    String contentType = file.getContentType();
    if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
      throw new IllegalArgumentException(
          "Invalid file type. Only JPEG, PNG, and GIF images are allowed");
    }
  }

  public void validateImage(MultipartFile file) {
    validateImage(file, DEFAULT_MAX_FILE_SIZE);
  }
}
