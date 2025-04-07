package com.altester.notification.repository;

import com.altester.notification.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUsernameOrderByCreatedAtDesc(String username);
    List<Notification> findByUsernameAndReadOrderByCreatedAtDesc(String username, boolean read);
    long countByUsernameAndRead(String username, boolean read);

    @Modifying
    int deleteByReadTrueAndCreatedAtBefore(LocalDateTime date);

    Page<Notification> findByUsernameOrderByCreatedAtDesc(String username, Pageable pageable);

    Page<Notification> findByUsernameAndReadOrderByCreatedAtDesc(String username, boolean read, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.username = :username AND " +
            "(LOWER(n.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(n.message) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "ORDER BY n.createdAt DESC")
    Page<Notification> searchByUsernameAndTerm(
            @Param("username") String username,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.username = :username AND n.read = :read AND " +
            "(LOWER(n.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(n.message) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "ORDER BY n.createdAt DESC")
    Page<Notification> searchByUsernameAndReadAndTerm(
            @Param("username") String username,
            @Param("read") boolean read,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);
}