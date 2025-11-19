package com.questgamification.service;

import com.questgamification.domain.entity.Achievement;
import com.questgamification.domain.entity.AchievementType;
import com.questgamification.domain.entity.User;
import com.questgamification.repository.AchievementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AchievementService {

    private static final Logger logger = LoggerFactory.getLogger(AchievementService.class);
    private final AchievementRepository achievementRepository;

    public AchievementService(AchievementRepository achievementRepository) {
        this.achievementRepository = achievementRepository;
    }

    @Cacheable(value = "achievements")
    public List<Achievement> getAllAchievements() {
        return achievementRepository.findAll();
    }

    @Cacheable(value = "achievements", key = "#type")
    public List<Achievement> getAchievementsByType(AchievementType type) {
        return achievementRepository.findByAchievementType(type);
    }

    @Transactional
    public void checkAndAwardAchievements(User user) {
        logger.info("Checking achievements for user {}", user.getUsername());
        
        List<Achievement> allAchievements = achievementRepository.findAll();
        
        for (Achievement achievement : allAchievements) {
            if (user.getAchievements().contains(achievement)) {
                continue;
            }

            boolean shouldAward = false;
            switch (achievement.getAchievementType()) {
                case QUESTS_COMPLETED:
                    shouldAward = user.getQuests().stream()
                        .filter(q -> q.getStatus().name().equals("COMPLETED"))
                        .count() >= achievement.getRequirementValue();
                    break;
                case TOTAL_XP_EARNED:
                    shouldAward = user.getExperiencePoints() >= achievement.getRequirementValue();
                    break;
                case LEVEL_REACHED:
                    shouldAward = user.getLevel() >= achievement.getRequirementValue();
                    break;
            }

            if (shouldAward) {
                user.getAchievements().add(achievement);
                achievement.getUsers().add(user);
                logger.info("Achievement '{}' awarded to user {}", achievement.getName(), user.getUsername());
            }
        }
    }
}

