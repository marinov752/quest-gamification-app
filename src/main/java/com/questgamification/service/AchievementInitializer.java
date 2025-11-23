package com.questgamification.service;

import com.questgamification.domain.entity.Achievement;
import com.questgamification.domain.entity.AchievementType;
import com.questgamification.repository.AchievementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Component
@Order(1)
public class AchievementInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AchievementInitializer.class);
    private final AchievementRepository achievementRepository;

    public AchievementInitializer(AchievementRepository achievementRepository) {
        this.achievementRepository = achievementRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        logger.info("Initializing basic achievements...");
        
        List<Achievement> basicAchievements = Arrays.asList(
            createAchievement("First Quest", "Complete your first quest", AchievementType.QUESTS_COMPLETED, 1),
            createAchievement("Quest Master", "Complete 5 quests", AchievementType.QUESTS_COMPLETED, 5),
            createAchievement("Quest Champion", "Complete 10 quests", AchievementType.QUESTS_COMPLETED, 10),
            createAchievement("Quest Legend", "Complete 25 quests", AchievementType.QUESTS_COMPLETED, 25),
            createAchievement("XP Starter", "Earn 100 XP", AchievementType.TOTAL_XP_EARNED, 100),
            createAchievement("XP Collector", "Earn 500 XP", AchievementType.TOTAL_XP_EARNED, 500),
            createAchievement("XP Master", "Earn 1000 XP", AchievementType.TOTAL_XP_EARNED, 1000),
            createAchievement("XP Legend", "Earn 5000 XP", AchievementType.TOTAL_XP_EARNED, 5000),
            createAchievement("Level Up", "Reach level 5", AchievementType.LEVEL_REACHED, 5),
            createAchievement("Level Master", "Reach level 10", AchievementType.LEVEL_REACHED, 10),
            createAchievement("Level Champion", "Reach level 20", AchievementType.LEVEL_REACHED, 20),
            createAchievement("Daily Dedication", "Complete 7 daily quests", AchievementType.STREAK_DAYS, 7),
            createAchievement("Weekly Warrior", "Complete 4 weekly quests", AchievementType.QUESTS_COMPLETED, 4)
        );

        for (Achievement achievement : basicAchievements) {
            if (!achievementRepository.findByAchievementType(achievement.getAchievementType())
                    .stream()
                    .anyMatch(a -> a.getName().equals(achievement.getName()))) {
                achievementRepository.save(achievement);
                logger.info("Created achievement: {}", achievement.getName());
            } else {
                logger.debug("Achievement already exists: {}", achievement.getName());
            }
        }
        
        logger.info("Basic achievements initialization completed. Total: {}", achievementRepository.count());
    }

    private Achievement createAchievement(String name, String description, AchievementType type, Integer requirement) {
        Achievement achievement = new Achievement();
        achievement.setName(name);
        achievement.setDescription(description);
        achievement.setAchievementType(type);
        achievement.setRequirementValue(requirement);
        return achievement;
    }
}

