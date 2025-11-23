package com.questgamification.repository;

import com.questgamification.domain.entity.Notification;
import com.questgamification.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    
    @Query("SELECT DISTINCT n FROM Notification n LEFT JOIN FETCH n.quest WHERE n.user = :user ORDER BY n.createdAt DESC")
    List<Notification> findByUserOrderByCreatedAtDesc(@Param("user") User user);
    
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);
    
    long countByUserAndIsReadFalse(User user);
    
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user = :user AND (n.isRead = false OR n.isRead IS NULL)")
    void markAllAsRead(@Param("user") User user);
    
    void deleteByUser(User user);
}

