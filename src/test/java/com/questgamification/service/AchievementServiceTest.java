package com.questgamification.service;

import com.questgamification.domain.entity.*;
import com.questgamification.repository.AchievementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AchievementServiceTest {

    @Mock
    private AchievementRepository achievementRepository;

    @InjectMocks
    private AchievementService achievementService;

    private User testUser;
    private Achievement testAchievement;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setLevel(5);
        testUser.setExperiencePoints(1000L);
        testUser.setAchievements(new HashSet<>());
        testUser.setQuests(new HashSet<>());

        testAchievement = new Achievement();
        testAchievement.setId(UUID.randomUUID());
        testAchievement.setName("Test Achievement");
        testAchievement.setDescription("Test Description");
        testAchievement.setAchievementType(AchievementType.LEVEL_REACHED);
        testAchievement.setRequirementValue(3);
        testAchievement.setUsers(new HashSet<>());
    }

    @Test
    void testGetAllAchievements() {
        List<Achievement> achievements = Arrays.asList(testAchievement);
        when(achievementRepository.findAll()).thenReturn(achievements);

        List<Achievement> result = achievementService.getAllAchievements();

        assertEquals(1, result.size());
        assertEquals(testAchievement.getName(), result.get(0).getName());
    }

    @Test
    void testGetAchievementsByType() {
        List<Achievement> achievements = Arrays.asList(testAchievement);
        when(achievementRepository.findByAchievementType(AchievementType.LEVEL_REACHED)).thenReturn(achievements);

        List<Achievement> result = achievementService.getAchievementsByType(AchievementType.LEVEL_REACHED);

        assertEquals(1, result.size());
        assertEquals(AchievementType.LEVEL_REACHED, result.get(0).getAchievementType());
    }

    @Test
    void testCheckAndAwardAchievements_LevelReached() {
        when(achievementRepository.findAll()).thenReturn(Arrays.asList(testAchievement));

        achievementService.checkAndAwardAchievements(testUser);

        assertTrue(testUser.getAchievements().contains(testAchievement));
    }

    @Test
    void testCheckAndAwardAchievements_TotalXpEarned() {
        testAchievement.setAchievementType(AchievementType.TOTAL_XP_EARNED);
        testAchievement.setRequirementValue(500);
        when(achievementRepository.findAll()).thenReturn(Arrays.asList(testAchievement));

        achievementService.checkAndAwardAchievements(testUser);

        assertTrue(testUser.getAchievements().contains(testAchievement));
    }

    @Test
    void testCheckAndAwardAchievements_QuestsCompleted() {
        Quest completedQuest = new Quest();
        completedQuest.setStatus(QuestStatus.COMPLETED);
        testUser.getQuests().add(completedQuest);

        testAchievement.setAchievementType(AchievementType.QUESTS_COMPLETED);
        testAchievement.setRequirementValue(1);
        when(achievementRepository.findAll()).thenReturn(Arrays.asList(testAchievement));

        achievementService.checkAndAwardAchievements(testUser);

        assertTrue(testUser.getAchievements().contains(testAchievement));
    }

    @Test
    void testCheckAndAwardAchievements_AlreadyUnlocked() {
        testUser.getAchievements().add(testAchievement);
        when(achievementRepository.findAll()).thenReturn(Arrays.asList(testAchievement));

        achievementService.checkAndAwardAchievements(testUser);

        assertEquals(1, testUser.getAchievements().size());
    }
}