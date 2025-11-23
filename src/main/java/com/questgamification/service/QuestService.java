package com.questgamification.service;

import com.questgamification.domain.dto.QuestCreateDto;
import com.questgamification.domain.dto.QuestProgressUpdateDto;
import com.questgamification.domain.entity.Quest;
import com.questgamification.domain.entity.QuestProgress;
import com.questgamification.domain.entity.QuestStatus;
import com.questgamification.domain.entity.User;
import com.questgamification.domain.entity.CheckIn;
import com.questgamification.repository.CheckInRepository;
import com.questgamification.repository.QuestProgressRepository;
import com.questgamification.repository.QuestRepository;
import com.questgamification.repository.RewardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class QuestService {

    private static final Logger logger = LoggerFactory.getLogger(QuestService.class);
    private final QuestRepository questRepository;
    private final QuestProgressRepository questProgressRepository;
    private final UserService userService;
    private final QuestAnalyticsClient questAnalyticsClient;
    private final AchievementService achievementService;
    private final NotificationService notificationService;
    private final RewardRepository rewardRepository;
    private final CheckInRepository checkInRepository;

    public QuestService(QuestRepository questRepository, 
                       QuestProgressRepository questProgressRepository,
                       UserService userService,
                       QuestAnalyticsClient questAnalyticsClient,
                       AchievementService achievementService,
                       NotificationService notificationService,
                       RewardRepository rewardRepository,
                       CheckInRepository checkInRepository) {
        this.questRepository = questRepository;
        this.questProgressRepository = questProgressRepository;
        this.userService = userService;
        this.questAnalyticsClient = questAnalyticsClient;
        this.achievementService = achievementService;
        this.notificationService = notificationService;
        this.rewardRepository = rewardRepository;
        this.checkInRepository = checkInRepository;
    }

    @Transactional
    @CacheEvict(value = "quests", allEntries = true)
    public Quest createQuest(QuestCreateDto questDto, User user) {
        logger.info("Creating quest '{}' for user {}", questDto.getTitle(), user.getUsername());
        
        if (questDto.getEndDate().isBefore(questDto.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        Quest quest = new Quest();
        quest.setTitle(questDto.getTitle());
        quest.setDescription(questDto.getDescription());
        quest.setQuestType(questDto.getQuestType());
        quest.setExperienceReward(questDto.getExperienceReward());
        quest.setStartDate(questDto.getStartDate());
        quest.setEndDate(questDto.getEndDate());
        quest.setCheckInGoal(questDto.getCheckInGoal());
        quest.setStatus(QuestStatus.ACTIVE);
        quest.setUser(user);

        if (questDto.getRewardIds() != null && !questDto.getRewardIds().isEmpty()) {
            java.util.Set<com.questgamification.domain.entity.Reward> rewards = new java.util.HashSet<>();
            for (UUID rewardId : questDto.getRewardIds()) {
                rewardRepository.findById(rewardId).ifPresent(rewards::add);
            }
            quest.setRewards(rewards);
            logger.info("Assigned {} rewards to quest", rewards.size());
        }
        
        Quest savedQuest = questRepository.save(quest);
        
        QuestProgress progress = new QuestProgress();
        progress.setQuest(savedQuest);
        progress.setUser(user);
        progress.setProgressPercentage(0);
        progress.setLastUpdated(LocalDateTime.now());
        questProgressRepository.save(progress);

        logger.info("Quest created successfully with ID: {}", savedQuest.getId());
        return savedQuest;
    }

    @Cacheable(value = "quests", key = "#id")
    public Optional<Quest> findById(UUID id) {
        return questRepository.findById(id);
    }

    @Cacheable(value = "quests", key = "'user_' + #user.id")
    public List<Quest> findByUser(User user) {
        return questRepository.findByUser(user);
    }

    @Cacheable(value = "quests", key = "'user_' + #user.id + '_status_' + #status")
    public List<Quest> findByUserAndStatus(User user, QuestStatus status) {
        return questRepository.findByUserAndStatus(user, status);
    }

    @Transactional
    @CacheEvict(value = "quests", allEntries = true)
    public CheckIn checkIn(UUID questId, User user) {
        logger.info("Processing check-in for quest {} by user {}", questId, user.getUsername());
        
        Quest quest = questRepository.findById(questId)
            .orElseThrow(() -> new IllegalArgumentException("Quest not found"));

        if (!quest.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("User does not own this quest");
        }

        if (quest.getStatus() != QuestStatus.ACTIVE) {
            throw new IllegalArgumentException("Quest is not active");
        }

        LocalDate today = LocalDate.now();
        
        if (quest.getQuestType() == com.questgamification.domain.entity.QuestType.DAILY) {
            Optional<CheckIn> existingCheckIn = checkInRepository.findByQuestAndUserAndCheckInDate(quest, user, today);
            if (existingCheckIn.isPresent()) {
                throw new IllegalArgumentException("You have already checked in today for this daily quest");
            }
        } else if (quest.getQuestType() == com.questgamification.domain.entity.QuestType.WEEKLY) {
            java.time.temporal.WeekFields weekFields = java.time.temporal.WeekFields.of(java.util.Locale.getDefault());
            int currentWeek = today.get(weekFields.weekOfYear());
            int currentYear = today.get(weekFields.weekBasedYear());
            
            List<CheckIn> checkInsThisWeek = checkInRepository.findByQuestAndUser(quest, user)
                .stream()
                .filter(checkIn -> {
                    int checkInWeek = checkIn.getCheckInDate().get(weekFields.weekOfYear());
                    int checkInYear = checkIn.getCheckInDate().get(weekFields.weekBasedYear());
                    return checkInWeek == currentWeek && checkInYear == currentYear;
                })
                .toList();
            
            if (!checkInsThisWeek.isEmpty()) {
                throw new IllegalArgumentException("You have already checked in this week for this weekly quest");
            }
        }

        int goal = quest.getCheckInGoal();
        long xpPerCheckIn = quest.getExperienceReward() / goal;
        
        try {
            User updatedUser = userService.addExperience(user, xpPerCheckIn);
            userService.updateUser(updatedUser);
            logger.info("Awarded {} XP to user {} for check-in", xpPerCheckIn, updatedUser.getUsername());
            
            try {
                achievementService.checkAndAwardAchievements(updatedUser);
                userService.updateUser(updatedUser);
            } catch (Exception e) {
                logger.warn("Failed to check achievements after check-in: {}", e.getMessage());
            }
        } catch (Exception e) {
            logger.error("Failed to add experience for check-in: {}", e.getMessage());
        }
        
        CheckIn checkIn = new CheckIn(quest, user);
        checkIn.setCheckInDate(today);
        checkIn = checkInRepository.save(checkIn);

        QuestProgress progress = questProgressRepository.findByQuestAndUser(quest, user)
            .orElseGet(() -> {
                QuestProgress newProgress = new QuestProgress();
                newProgress.setQuest(quest);
                newProgress.setUser(user);
                newProgress.setProgressPercentage(0);
                newProgress.setLastUpdated(LocalDateTime.now());
                return questProgressRepository.save(newProgress);
            });
        
        long totalCheckIns = checkInRepository.countByQuestAndUser(quest, user);
        
        int progressPercentage = (int) Math.min(100, (totalCheckIns * 100.0 / goal));
        progress.setProgressPercentage(progressPercentage);
        progress.setLastUpdated(LocalDateTime.now());
        questProgressRepository.save(progress);

        if (totalCheckIns >= goal) {
            completeQuest(quest, user);
        }

        logger.info("Check-in recorded successfully for quest {} by user {}", questId, user.getUsername());
        return checkIn;
    }

    public List<CheckIn> getCheckInsForQuest(Quest quest, User user) {
        return checkInRepository.findByQuestAndUserOrderByCheckInDateDesc(quest, user);
    }
    
    public List<Quest> getQuestsReadyForCheckIn(User user) {
        LocalDate today = LocalDate.now();
        List<Quest> activeQuests = findByUserAndStatus(user, QuestStatus.ACTIVE);
        
        return activeQuests.stream()
            .filter(quest -> canCheckIn(quest, user, today))
            .toList();
    }

    public boolean canCheckIn(Quest quest, User user, LocalDate date) {
        if (quest.getStatus() != QuestStatus.ACTIVE) {
            return false;
        }
        
        if (!quest.getUser().getId().equals(user.getId())) {
            return false;
        }
        
        if (quest.getQuestType() == com.questgamification.domain.entity.QuestType.DAILY) {
            Optional<CheckIn> existingCheckIn = checkInRepository.findByQuestAndUserAndCheckInDate(quest, user, date);
            return existingCheckIn.isEmpty();
        } else if (quest.getQuestType() == com.questgamification.domain.entity.QuestType.WEEKLY) {
            java.time.temporal.WeekFields weekFields = java.time.temporal.WeekFields.of(java.util.Locale.getDefault());
            int currentWeek = date.get(weekFields.weekOfYear());
            int currentYear = date.get(weekFields.weekBasedYear());
            
            List<CheckIn> checkInsThisWeek = checkInRepository.findByQuestAndUser(quest, user)
                .stream()
                .filter(checkIn -> {
                    int checkInWeek = checkIn.getCheckInDate().get(weekFields.weekOfYear());
                    int checkInYear = checkIn.getCheckInDate().get(weekFields.weekBasedYear());
                    return checkInWeek == currentWeek && checkInYear == currentYear;
                })
                .toList();
            
            return checkInsThisWeek.isEmpty();
        }
        
        return false;
    }

    @Transactional
    @CacheEvict(value = "quests", allEntries = true)
    public QuestProgress updateProgress(QuestProgressUpdateDto progressDto, User user) {
        logger.warn("updateProgress called - this method is deprecated. Use checkIn instead.");
        throw new UnsupportedOperationException("Please use the check-in system instead. Visit the quest details page to check in.");
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "quests", allEntries = true),
        @CacheEvict(value = "stats", key = "'user_' + #user.id"),
        @CacheEvict(value = "users", key = "#user.id")
    })
    public void completeQuest(Quest quest, User user) {
        logger.info("Completing quest {} for user {}", quest.getId(), user.getUsername());
        
        if (quest.getStatus() != QuestStatus.ACTIVE) {
            throw new IllegalArgumentException("Quest is not active");
        }

        try {
            quest.setStatus(QuestStatus.COMPLETED);
            questRepository.save(quest);

            User updatedUser = userService.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            try {
                achievementService.checkAndAwardAchievements(updatedUser);
                userService.updateUser(updatedUser);
            } catch (Exception e) {
                logger.warn("Failed to check achievements for user {}: {}", user.getUsername(), e.getMessage());
            }
            
            try {
                long totalXpGiven = quest.getExperienceReward();
                questAnalyticsClient.recordQuestCompletion(user.getId(), quest.getId(), totalXpGiven);
            } catch (Exception e) {
                logger.warn("Failed to record quest completion in analytics service for quest {}: {}", quest.getId(), e.getMessage());
            }
            
            try {
                notificationService.createQuestCompletedNotification(quest);
            } catch (Exception e) {
                logger.warn("Failed to create completion notification for quest {}: {}", quest.getId(), e.getMessage());
            }
            
            logger.info("Quest {} completed successfully", quest.getId());
        } catch (IllegalArgumentException e) {
            logger.error("Error completing quest {}: {}", quest.getId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error completing quest {}: {}", quest.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to complete quest: " + e.getMessage(), e);
        }
    }

    @Transactional
    @CacheEvict(value = "quests", allEntries = true)
    public void deleteQuest(UUID questId, User user) {
        logger.info("Deleting quest {} by user {}", questId, user.getUsername());
        
        Quest quest = questRepository.findById(questId)
            .orElseThrow(() -> new IllegalArgumentException("Quest not found"));

        if (!quest.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("User does not own this quest");
        }

        questRepository.delete(quest);
        logger.info("Quest {} deleted successfully", questId);
    }

    public List<Quest> findExpiredQuests() {
        return questRepository.findByStatusAndEndDateBefore(QuestStatus.ACTIVE, LocalDate.now());
    }

    @Transactional
    @CacheEvict(value = "quests", allEntries = true)
    public void expireQuests(List<Quest> expiredQuests) {
        logger.info("Expiring {} quests", expiredQuests.size());
        expiredQuests.forEach(quest -> quest.setStatus(QuestStatus.EXPIRED));
        questRepository.saveAll(expiredQuests);
    }
}

