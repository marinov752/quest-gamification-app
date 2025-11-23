package com.questgamification.scheduler;

import com.questgamification.domain.entity.CheckIn;
import com.questgamification.domain.entity.Quest;
import com.questgamification.domain.entity.QuestStatus;
import com.questgamification.domain.entity.QuestType;
import com.questgamification.repository.CheckInRepository;
import com.questgamification.repository.QuestRepository;
import com.questgamification.service.NotificationService;
import com.questgamification.service.QuestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Optional;
import java.util.Locale;

@Component
public class QuestScheduler {

    private static final Logger logger = LoggerFactory.getLogger(QuestScheduler.class);
    private final QuestService questService;
    private final QuestRepository questRepository;
    private final NotificationService notificationService;
    private final CheckInRepository checkInRepository;

    public QuestScheduler(QuestService questService, QuestRepository questRepository, 
                         NotificationService notificationService, CheckInRepository checkInRepository) {
        this.questService = questService;
        this.questRepository = questRepository;
        this.notificationService = notificationService;
        this.checkInRepository = checkInRepository;
    }

    @Scheduled(cron = "0 0 0 * * *")
    @CacheEvict(value = {"notifications", "notificationCount"}, allEntries = true)
    public void expireQuestsDaily() {
        logger.info("Running daily quest expiration job at {}", LocalDateTime.now());
        var expiredQuests = questService.findExpiredQuests();
        if (!expiredQuests.isEmpty()) {
            questService.expireQuests(expiredQuests);
            expiredQuests.forEach(quest -> notificationService.createQuestExpiredNotification(quest));
            logger.info("Expired {} quests", expiredQuests.size());
        }
    }

    @Scheduled(fixedDelay = 3600000)
    public void expireQuestsHourly() {
        logger.info("Running hourly quest expiration check at {}", LocalDateTime.now());
        var expiredQuests = questService.findExpiredQuests();
        if (!expiredQuests.isEmpty()) {
            questService.expireQuests(expiredQuests);
            expiredQuests.forEach(quest -> notificationService.createQuestExpiredNotification(quest));
            logger.info("Expired {} quests", expiredQuests.size());
        }
    }

    @Scheduled(cron = "0 0 9 * * *")
    @CacheEvict(value = {"notifications", "notificationCount"}, allEntries = true)
    public void sendMorningQuestReminders() {
        logger.info("Running morning quest reminder job at {}", LocalDateTime.now());
        LocalDate today = LocalDate.now();
        List<Quest> activeQuests = questRepository.findAll().stream()
            .filter(q -> q.getStatus() == QuestStatus.ACTIVE)
            .toList();
        
        for (Quest quest : activeQuests) {
            LocalDate endDate = quest.getEndDate();
            long daysUntilDeadline = ChronoUnit.DAYS.between(today, endDate);
            
            if (daysUntilDeadline < 0) {
                continue; // Quest already expired, skip
            }
            
            if (quest.getQuestType() == QuestType.DAILY) {
                notificationService.createQuestReminderNotification(quest, (int) daysUntilDeadline);
                logger.info("Sent morning reminder for DAILY quest {} ({} days remaining)", quest.getId(), daysUntilDeadline);
            }
            
            if (quest.getQuestType() == QuestType.WEEKLY) {
                java.time.DayOfWeek dayOfWeek = today.getDayOfWeek();
                if (dayOfWeek == java.time.DayOfWeek.MONDAY) {
                    notificationService.createQuestReminderNotification(quest, (int) daysUntilDeadline);
                    logger.info("Sent weekly reminder for WEEKLY quest {} ({} days remaining)", quest.getId(), daysUntilDeadline);
                }
            }
            
            if (daysUntilDeadline == 1) {
                notificationService.createQuestExpiringNotification(quest);
                logger.info("Sent expiring soon notification for quest {}", quest.getId());
            }
        }
        
        logger.info("Completed morning quest reminder job. Processed {} active quests", activeQuests.size());
    }

    @Scheduled(cron = "0 0 19 * * *")
    @CacheEvict(value = {"notifications", "notificationCount"}, allEntries = true)
    public void sendEveningQuestReminders() {
        logger.info("Running evening quest reminder job at {}", LocalDateTime.now());
        LocalDate today = LocalDate.now();
        List<Quest> activeQuests = questRepository.findAll().stream()
            .filter(q -> q.getStatus() == QuestStatus.ACTIVE)
            .toList();
        
        for (Quest quest : activeQuests) {
            LocalDate endDate = quest.getEndDate();
            long daysUntilDeadline = ChronoUnit.DAYS.between(today, endDate);
            
            if (daysUntilDeadline < 0) {
                continue;
            }
            
            if (quest.getQuestType() == QuestType.DAILY) {
                Optional<CheckIn> todayCheckIn = checkInRepository.findByQuestAndUserAndCheckInDate(
                    quest, quest.getUser(), today);
                
                if (todayCheckIn.isEmpty() && questService.canCheckIn(quest, quest.getUser(), today)) {
                    String message = String.format("Don't forget to check in for your daily quest '%s' today! Deadline: %s",
                        quest.getTitle(), endDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    notificationService.createNotification(quest.getUser(), quest, 
                        com.questgamification.domain.entity.NotificationType.QUEST_REMINDER, message);
                    logger.info("Sent evening reminder for uncheck-in DAILY quest {}", quest.getId());
                }
            }
            
            if (quest.getQuestType() == QuestType.WEEKLY && today.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
                WeekFields weekFields = WeekFields.of(Locale.getDefault());
                int currentWeek = today.get(weekFields.weekOfYear());
                int currentYear = today.get(weekFields.weekBasedYear());
                
                List<CheckIn> checkInsThisWeek = checkInRepository.findByQuestAndUser(quest, quest.getUser())
                    .stream()
                    .filter(checkIn -> {
                        int checkInWeek = checkIn.getCheckInDate().get(weekFields.weekOfYear());
                        int checkInYear = checkIn.getCheckInDate().get(weekFields.weekBasedYear());
                        return checkInWeek == currentWeek && checkInYear == currentYear;
                    })
                    .toList();
                
                if (checkInsThisWeek.isEmpty() && questService.canCheckIn(quest, quest.getUser(), today)) {
                    String message = String.format("Don't forget to check in for your weekly quest '%s' this week! Deadline: %s",
                        quest.getTitle(), endDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    notificationService.createNotification(quest.getUser(), quest, 
                        com.questgamification.domain.entity.NotificationType.QUEST_REMINDER, message);
                    logger.info("Sent Sunday evening reminder for uncheck-in WEEKLY quest {}", quest.getId());
                }
            }
        }
        
        logger.info("Completed evening quest reminder job. Processed {} active quests", activeQuests.size());
    }

    @Scheduled(cron = "0 0 14 * * *")
    @CacheEvict(value = {"notifications", "notificationCount"}, allEntries = true)
    public void sendMiddayQuestReminders() {
        logger.info("Running midday quest reminder job at {}", LocalDateTime.now());
        LocalDate today = LocalDate.now();
        List<Quest> activeQuests = questRepository.findAll().stream()
            .filter(q -> q.getStatus() == QuestStatus.ACTIVE)
            .toList();
        
        for (Quest quest : activeQuests) {
            LocalDate endDate = quest.getEndDate();
            long daysUntilDeadline = ChronoUnit.DAYS.between(today, endDate);
            
            if (daysUntilDeadline < 0) {
                continue;
            }
            
            if (daysUntilDeadline >= 2 && daysUntilDeadline <= 3) {
                notificationService.createQuestExpiringNotification(quest);
                logger.info("Sent midday expiring soon reminder for quest {} ({} days remaining)", quest.getId(), daysUntilDeadline);
            }
        }
        
        logger.info("Completed midday quest reminder job. Processed {} active quests", activeQuests.size());
    }

    @Scheduled(cron = "0 0 18 * * 0")
    @CacheEvict(value = {"notifications", "notificationCount"}, allEntries = true)
    public void sendWeeklyProgressSummary() {
        logger.info("Running weekly progress summary job at {}", LocalDateTime.now());
        
        List<Quest> activeQuests = questRepository.findAll().stream()
            .filter(q -> q.getStatus() == QuestStatus.ACTIVE)
            .toList();
        
        for (Quest quest : activeQuests) {
            List<CheckIn> checkIns = checkInRepository.findByQuestAndUser(quest, quest.getUser());
            long totalCheckIns = checkIns.size();
            int goal = quest.getCheckInGoal();
            int progressPercent = (int) Math.min(100, (totalCheckIns * 100.0 / goal));
            
            String message = String.format(
                "Weekly Progress Update for '%s': %d/%d check-ins completed (%d%%). Deadline: %s",
                quest.getTitle(), totalCheckIns, goal, progressPercent,
                quest.getEndDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            );
            
            notificationService.createNotification(quest.getUser(), quest, 
                com.questgamification.domain.entity.NotificationType.SYSTEM, message);
            logger.info("Sent weekly progress summary for quest {}", quest.getId());
        }
        
        logger.info("Completed weekly progress summary job. Processed {} active quests", activeQuests.size());
    }
}

