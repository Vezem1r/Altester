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

    @Modifying
    int deleteByReadTrueAndCreatedAtBefore(LocalDateTime date);

    @Query("SELECT n FROM Notification n WHERE n.username = :username AND n.read = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadNotifications(@Param("username") String username);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.username = :username AND n.read = false")
    long countUnreadNotifications(@Param("username") String username);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.username = :username AND n.read = false")
    void markAllAsRead(@Param("username") String username);

    @Query("SELECT n FROM Notification n WHERE n.username = :username AND (n.read = :read) ORDER BY n.createdAt DESC")
    Page<Notification> findByUsernameAndRead(@Param("username") String username, @Param("read") boolean read, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.username = :username ORDER BY n.createdAt DESC")
    Page<Notification> findByUsername(@Param("username") String username, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.username = :username " +
            "AND (n.read = :read) " +
            "AND (LOWER(n.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(n.message) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY n.createdAt DESC")
    Page<Notification> findByUsernameAndReadAndSearchTerm(
            @Param("username") String username,
            @Param("read") boolean read,
            @Param("search") String search,
            Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.username = :username " +
            "AND (LOWER(n.title) " +
            "LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(n.message) " +
            "LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY n.createdAt DESC")
    Page<Notification> findByUsernameAndSearchTerm(
            @Param("username") String username,
            @Param("search") String search,
            Pageable pageable);
}