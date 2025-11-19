package com.questgamification.service;

import com.questgamification.domain.dto.QuestCreateDto;
import com.questgamification.domain.dto.QuestProgressUpdateDto;
import com.questgamification.domain.entity.Quest;
import com.questgamification.domain.entity.QuestProgress;
import com.questgamification.domain.entity.QuestStatus;
import com.questgamification.domain.entity.User;
import com.questgamification.repository.QuestProgressRepository;
import com.questgamification.repository.QuestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    public QuestService(QuestRepository questRepository, 
                       QuestProgressRepository questProgressRepository,
                       UserService userService,
                       QuestAnalyticsClient questAnalyticsClient,
                       AchievementService achievementService) {
        this.questRepository = questRepository;
        this.questProgressRepository = questProgressRepository;
        this.userService = userService;
        this.questAnalyticsClient = questAnalyticsClient;
        this.achievementService = achievementService;
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
        quest.setStatus(QuestStatus.ACTIVE);
        quest.setUser(user);

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
    public QuestProgress updateProgress(QuestProgressUpdateDto progressDto, User user) {
        logger.info("Updating progress for quest {} to {}%", progressDto.getQuestId(), progressDto.getProgressPercentage());
        
        Quest quest = questRepository.findById(progressDto.getQuestId())
            .orElseThrow(() -> new IllegalArgumentException("Quest not found"));

        if (!quest.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("User does not own this quest");
        }

        if (quest.getStatus() != QuestStatus.ACTIVE) {
            throw new IllegalArgumentException("Quest is not active");
        }

        QuestProgress progress = questProgressRepository.findByQuestAndUser(quest, user)
            .orElseThrow(() -> new IllegalArgumentException("Progress record not found"));

        progress.setProgressPercentage(progressDto.getProgressPercentage());
        progress.setLastUpdated(LocalDateTime.now());

        if (progressDto.getProgressPercentage() >= 100) {
            completeQuest(quest, user);
        }

        return questProgressRepository.save(progress);
    }

    @Transactional
    @CacheEvict(value = "quests", allEntries = true)
    public void completeQuest(Quest quest, User user) {
        logger.info("Completing quest {} for user {}", quest.getId(), user.getUsername());
        
        if (quest.getStatus() != QuestStatus.ACTIVE) {
            throw new IllegalArgumentException("Quest is not active");
        }

        quest.setStatus(QuestStatus.COMPLETED);
        questRepository.save(quest);

        User updatedUser = userService.addExperience(user, quest.getExperienceReward());
        achievementService.checkAndAwardAchievements(updatedUser);
        userService.updateUser(updatedUser);
        
        questAnalyticsClient.recordQuestCompletion(user.getId(), quest.getId(), quest.getExperienceReward());
        
        logger.info("Quest {} completed successfully", quest.getId());
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

