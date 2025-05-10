package com.altester.core.config;

import com.altester.core.exception.FileOperationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class QuestionImageConfig {
  @Value("${app.upload.question-images}")
  private String uploadDir;

  @Bean
  public Path questionImagesDirectory() {
    try {
      Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

      if (!Files.exists(uploadPath)) {
        Files.createDirectories(uploadPath);
      }

      setPosixPermissions(uploadPath);

      return uploadPath;
    } catch (IOException e) {
      throw FileOperationException.directoryCreationError("Could not create upload directory");
    }
  }

  private void setPosixPermissions(Path path) {
    try {
      Set<PosixFilePermission> perms = new HashSet<>();
      perms.add(PosixFilePermission.OWNER_READ);
      perms.add(PosixFilePermission.OWNER_WRITE);
      perms.add(PosixFilePermission.OWNER_EXECUTE);
      perms.add(PosixFilePermission.GROUP_READ);
      perms.add(PosixFilePermission.GROUP_WRITE);
      perms.add(PosixFilePermission.GROUP_EXECUTE);
      perms.add(PosixFilePermission.OTHERS_READ);
      perms.add(PosixFilePermission.OTHERS_WRITE);
      perms.add(PosixFilePermission.OTHERS_EXECUTE);

      Files.setPosixFilePermissions(path, perms);
    } catch (IOException e) {
      log.warn("Could not set POSIX permissions: {}", e.getMessage());
    }
  }
}
