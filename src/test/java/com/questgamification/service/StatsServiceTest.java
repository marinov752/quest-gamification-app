package com.questgamification.service;

import com.questgamification.domain.entity.*;
import com.questgamification.repository.QuestRepository;
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
class StatsServiceTest {

    @Mock
    private QuestRepository questRepository;

    @Mock
    private QuestAnalyticsClient questAnalyticsClient;

    @InjectMocks
    private StatsService statsService;

    private User testUser;
    private Quest activeQuest;
    private Quest completedQuest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setLevel(5);
        testUser.setExperiencePoints(1000L);
        testUser.setAchievements(new HashSet<>());
        testUser.setClaimedRewards(new HashSet<>());

        activeQuest = new Quest();
        activeQuest.setStatus(QuestStatus.ACTIVE);

        completedQuest = new Quest();
        completedQuest.setStatus(QuestStatus.COMPLETED);
    }

    @Test
    void testGetUserStats() {
        List<Quest> quests = Arrays.asList(activeQuest, completedQuest);
        when(questRepository.findByUser(testUser)).thenReturn(quests);

        Map<String, Object> stats = statsService.getUserStats(testUser);

        assertEquals(5, stats.get("level"));
        assertEquals(1000L, stats.get("experiencePoints"));
        assertEquals(2, stats.get("totalQuests"));
        assertEquals(1L, stats.get("completedQuests"));
        assertEquals(1L, stats.get("activeQuests"));
        assertEquals(0, stats.get("achievementsCount"));
        assertEquals(0, stats.get("rewardsClaimed"));
    }

    @Test
    void testGetAnalyticsData() {
        Map<String, Object> analyticsData = new HashMap<>();
        analyticsData.put("totalExperienceEarned", 1000L);
        analyticsData.put("totalQuestsCompleted", 5);

        when(questAnalyticsClient.getAnalyticsData(testUser.getId())).thenReturn(analyticsData);

        Map<String, Object> result = statsService.getAnalyticsData(testUser.getId());

        assertEquals(1000L, result.get("totalExperienceEarned"));
        assertEquals(5, result.get("totalQuestsCompleted"));
    }
}