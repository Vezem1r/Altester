package com.altester.core.service.test;

import com.altester.core.exception.FileOperationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class ImageService {

    @Value("${app.upload.question-images}")
    private String uploadDir;

    /**
     * Saves an uploaded image file to the file system.
     *
     * @param image The image file to save
     * @return The filename of the saved image
     * @throws FileOperationException If there's an error saving the image
     */
    public String saveImage(MultipartFile image) {
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = image.getOriginalFilename();
            String fileExtension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";

            String filename = UUID.randomUUID() + fileExtension;
            Path filePath = uploadPath.resolve(filename);

            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            log.debug("Image saved successfully: {}", filename);

            return filename;
        } catch (IOException e) {
            log.error("Failed to save image", e);
            throw FileOperationException.imageSave("Failed to save image: " + e.getMessage());
        }
    }

    /**
     * Deletes an image from the file system.
     *
     * @param imagePath The path of the image to delete
     */
    public void deleteImage(String imagePath) {
        try {
            Path path = Paths.get(uploadDir, imagePath);
            boolean deleted = Files.deleteIfExists(path);
            if (deleted) {
                log.debug("Image deleted successfully: {}", imagePath);
            } else {
                log.warn("Image not found for deletion: {}", imagePath);
            }
        } catch (IOException e) {
            log.error("Failed to delete image: {}", e.getMessage());
        }
    }
}