package com.questgamification.service;

import com.questgamification.domain.entity.*;
import com.questgamification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public Notification createNotification(User user, Quest quest, NotificationType type, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setQuest(quest);
        notification.setType(type);
        notification.setMessage(message);
        notification.setIsRead(false);
        
        Notification saved = notificationRepository.save(notification);
        logger.info("Created notification {} for user {}", saved.getId(), user.getUsername());
        return saved;
    }

    @Transactional
    public void createQuestExpiringNotification(Quest quest) {
        String formattedDate = quest.getEndDate().format(DATE_FORMATTER);
        String message = String.format("Quest '%s' is expiring soon! Deadline: %s", 
            quest.getTitle(), formattedDate);
        createNotification(quest.getUser(), quest, NotificationType.QUEST_EXPIRING_SOON, message);
        logger.info("Created expiring notification for quest {}", quest.getId());
    }

    @Transactional
    public void createQuestExpiredNotification(Quest quest) {
        String formattedDate = quest.getEndDate().format(DATE_FORMATTER);
        String message = String.format("Quest '%s' has expired on %s", 
            quest.getTitle(), formattedDate);
        createNotification(quest.getUser(), quest, NotificationType.QUEST_EXPIRED, message);
        logger.info("Created expired notification for quest {}", quest.getId());
    }

    @Transactional
    public void createQuestCompletedNotification(Quest quest) {
        String message = String.format("Congratulations! You completed the quest '%s'! You earned a total of %d XP (given per check-in).", 
            quest.getTitle(), quest.getExperienceReward());
        createNotification(quest.getUser(), quest, NotificationType.QUEST_COMPLETED, message);
        logger.info("Created completion notification for quest {}", quest.getId());
    }

    @Transactional
    public void createQuestReminderNotification(Quest quest, int daysUntilDeadline) {
        String formattedDate = quest.getEndDate().format(DATE_FORMATTER);
        String message = String.format("Reminder: Quest '%s' deadline is in %d day(s)! (Deadline: %s)", 
            quest.getTitle(), daysUntilDeadline, formattedDate);
        createNotification(quest.getUser(), quest, NotificationType.QUEST_REMINDER, message);
        logger.info("Created reminder notification for quest {} ({} days remaining)", quest.getId(), daysUntilDeadline);
    }

    @Transactional
    public void createAchievementUnlockedNotification(User user, Achievement achievement) {
        String message = String.format("Achievement unlocked: '%s'! %s", 
            achievement.getName(), 
            achievement.getDescription() != null ? achievement.getDescription() : "Keep up the great work!");
        createNotification(user, null, NotificationType.ACHIEVEMENT_UNLOCKED, message);
        logger.info("Created achievement unlocked notification for user {} - achievement: {}", 
            user.getUsername(), achievement.getName());
    }

    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(User user) {
        if (user == null || user.getId() == null) {
            logger.warn("User or user ID is null, returning empty list");
            return Collections.emptyList();
        }
        
        logger.debug("Fetching notifications for user {} (ID: {})", user.getUsername(), user.getId());
        try {
            List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
            
            if (notifications == null) {
                logger.warn("Repository returned null for user {}", user.getUsername());
                return Collections.emptyList();
            }
            
            if (notifications.isEmpty()) {
                logger.debug("No notifications found for user {}", user.getUsername());
                return Collections.emptyList();
            }
            
            logger.debug("Found {} notifications, initializing lazy proxies", notifications.size());
            
            for (Notification n : notifications) {
                try {
                    if (n.getId() != null) {
                        n.getId();
                    }
                    if (n.getUser() != null) {
                        n.getUser().getId();
                    }
                    if (n.getQuest() != null) {
                        n.getQuest().getId();
                    }
                    if (n.getType() != null) {
                        n.getType().name();
                    }
                    if (n.getMessage() != null) {
                        n.getMessage().length();
                    }
                    if (n.getCreatedAt() != null) {
                        n.getCreatedAt().toString();
                    }
                    if (n.getIsRead() != null) {
                        n.getIsRead();
                    }
                } catch (Exception e) {
                    logger.error("Error initializing notification {}: {}", n != null && n.getId() != null ? n.getId() : "unknown", e.getMessage(), e);
                }
            }
            
            logger.debug("Successfully loaded and initialized {} notifications for user {}", notifications.size(), user.getUsername());
            return notifications;
        } catch (Exception e) {
            logger.error("Error fetching notifications for user {}: {}", user.getUsername(), e.getMessage(), e);
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(User user) {
        if (user == null) {
            return 0L;
        }
        
        try {
            long count = notificationRepository.countByUserAndIsReadFalse(user);
            logger.debug("Unread notifications count for user {}: {}", user.getUsername(), count);
            return count;
        } catch (Exception e) {
            logger.error("Error counting unread notifications for user {}: {}", user.getUsername(), e.getMessage(), e);
            return 0L;
        }
    }

    @Transactional
    @CacheEvict(value = {"notifications", "notificationCount"}, key = "'user_' + #user.id")
    public void markAsRead(UUID notificationId, User user) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            if (notification.getUser().getId().equals(user.getId())) {
                notification.setIsRead(true);
                notificationRepository.save(notification);
                logger.info("Marked notification {} as read", notificationId);
            }
        });
    }

    @Transactional
    @CacheEvict(value = {"notifications", "notificationCount"}, key = "'user_' + #user.id")
    public void markAllAsRead(User user) {
        notificationRepository.markAllAsRead(user);
        logger.info("Marked all notifications as read for user {}", user.getUsername());
    }

    @Transactional
    public void deleteNotification(UUID notificationId, User user) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            if (notification.getUser().getId().equals(user.getId())) {
                notificationRepository.delete(notification);
                logger.info("Deleted notification {} for user {}", notificationId, user.getUsername());
            }
        });
    }
}
