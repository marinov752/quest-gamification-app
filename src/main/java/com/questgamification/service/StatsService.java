package com.questgamification.service;

import com.questgamification.domain.entity.Quest;
import com.questgamification.domain.entity.QuestStatus;
import com.questgamification.domain.entity.User;
import com.questgamification.repository.QuestRepository;
import com.questgamification.service.QuestAnalyticsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class StatsService {

    private static final Logger logger = LoggerFactory.getLogger(StatsService.class);
    private final QuestRepository questRepository;
    private final QuestAnalyticsClient questAnalyticsClient;

    public StatsService(QuestRepository questRepository, QuestAnalyticsClient questAnalyticsClient) {
        this.questRepository = questRepository;
        this.questAnalyticsClient = questAnalyticsClient;
    }

    @Cacheable(value = "stats", key = "'user_' + #user.id")
    public Map<String, Object> getUserStats(User user) {
        logger.info("Retrieving stats for user {} (ID: {})", user.getUsername(), user.getId());

        List<Quest> allQuests = questRepository.findByUser(user);
        logger.debug("Found {} quests for user {}", allQuests.size(), user.getUsername());
        
        long completedQuests = allQuests.stream()
                .filter(q -> {
                    QuestStatus status = q.getStatus();
                    boolean isCompleted = status == QuestStatus.COMPLETED;
                    if (isCompleted) {
                        logger.debug("Found completed quest: {} - {}", q.getId(), q.getTitle());
                    }
                    return isCompleted;
                })
                .count();
        long activeQuests = allQuests.stream()
                .filter(q -> q.getStatus() == QuestStatus.ACTIVE)
                .count();
        long expiredQuests = allQuests.stream()
                .filter(q -> q.getStatus() == QuestStatus.EXPIRED)
                .count();

        logger.info("Stats for user {}: total={}, completed={}, active={}, expired={}", 
                    user.getUsername(), allQuests.size(), completedQuests, activeQuests, expiredQuests);

        Map<String, Object> stats = new HashMap<>();
        stats.put("level", user.getLevel());
        stats.put("experiencePoints", user.getExperiencePoints());
        stats.put("totalQuests", allQuests.size());
        stats.put("completedQuests", completedQuests);
        stats.put("activeQuests", activeQuests);
        stats.put("expiredQuests", expiredQuests);
        stats.put("achievementsCount", user.getAchievements().size());
        stats.put("rewardsClaimed", user.getClaimedRewards().size());

        return stats;
    }

    public Map<String, Object> getAnalyticsData(UUID userId) {
        logger.info("Retrieving analytics data for user {}", userId);
        try {
            return questAnalyticsClient.getAnalyticsData(userId);
        } catch (Exception e) {
            logger.warn("Failed to retrieve analytics data for user {}: {}", userId, e.getMessage());
            Map<String, Object> emptyAnalytics = new HashMap<>();
            emptyAnalytics.put("totalExperienceEarned", 0L);
            emptyAnalytics.put("totalQuestsCompleted", 0);
            emptyAnalytics.put("currentLevel", 1);
            emptyAnalytics.put("lastUpdated", null);
            return emptyAnalytics;
        }
    }
}
