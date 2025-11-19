package com.questgamification.service;

import com.questgamification.domain.dto.QuestCreateDto;
import com.questgamification.domain.dto.QuestProgressUpdateDto;
import com.questgamification.domain.entity.*;
import com.questgamification.repository.QuestProgressRepository;
import com.questgamification.repository.QuestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestServiceTest {

    @Mock
    private QuestRepository questRepository;

    @Mock
    private QuestProgressRepository questProgressRepository;

    @Mock
    private UserService userService;

    @Mock
    private QuestAnalyticsClient questAnalyticsClient;

    @Mock
    private AchievementService achievementService;

    @InjectMocks
    private QuestService questService;

    private User testUser;
    private Quest testQuest;
    private QuestProgress testProgress;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setLevel(1);
        testUser.setExperiencePoints(0L);

        testQuest = new Quest();
        testQuest.setId(UUID.randomUUID());
        testQuest.setTitle("Test Quest");
        testQuest.setDescription("Test Description");
        testQuest.setQuestType(QuestType.DAILY);
        testQuest.setStatus(QuestStatus.ACTIVE);
        testQuest.setExperienceReward(100L);
        testQuest.setStartDate(LocalDate.now());
        testQuest.setEndDate(LocalDate.now().plusDays(1));
        testQuest.setUser(testUser);

        testProgress = new QuestProgress();
        testProgress.setId(UUID.randomUUID());
        testProgress.setQuest(testQuest);
        testProgress.setUser(testUser);
        testProgress.setProgressPercentage(50);
        testProgress.setLastUpdated(LocalDateTime.now());
    }

    @Test
    void testCreateQuest_Success() {
        QuestCreateDto dto = new QuestCreateDto();
        dto.setTitle("New Quest");
        dto.setDescription("Description");
        dto.setQuestType(QuestType.DAILY);
        dto.setExperienceReward(100L);
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(1));

        when(questRepository.save(any(Quest.class))).thenAnswer(invocation -> {
            Quest quest = invocation.getArgument(0);
            quest.setId(UUID.randomUUID());
            return quest;
        });
        when(questProgressRepository.save(any(QuestProgress.class))).thenReturn(testProgress);

        Quest result = questService.createQuest(dto, testUser);

        assertNotNull(result);
        verify(questRepository, times(1)).save(any(Quest.class));
        verify(questProgressRepository, times(1)).save(any(QuestProgress.class));
    }

    @Test
    void testCreateQuest_EndDateBeforeStartDate() {
        QuestCreateDto dto = new QuestCreateDto();
        dto.setTitle("New Quest");
        dto.setQuestType(QuestType.DAILY);
        dto.setExperienceReward(100L);
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().minusDays(1));

        assertThrows(IllegalArgumentException.class, () -> {
            questService.createQuest(dto, testUser);
        });
    }

    @Test
    void testFindById() {
        when(questRepository.findById(testQuest.getId())).thenReturn(Optional.of(testQuest));

        Optional<Quest> result = questService.findById(testQuest.getId());

        assertTrue(result.isPresent());
        assertEquals(testQuest.getId(), result.get().getId());
    }

    @Test
    void testFindByUser() {
        List<Quest> quests = Arrays.asList(testQuest);
        when(questRepository.findByUser(testUser)).thenReturn(quests);

        List<Quest> result = questService.findByUser(testUser);

        assertEquals(1, result.size());
        assertEquals(testQuest.getId(), result.get(0).getId());
    }

    @Test
    void testFindByUserAndStatus() {
        List<Quest> quests = Arrays.asList(testQuest);
        when(questRepository.findByUserAndStatus(testUser, QuestStatus.ACTIVE)).thenReturn(quests);

        List<Quest> result = questService.findByUserAndStatus(testUser, QuestStatus.ACTIVE);

        assertEquals(1, result.size());
        assertEquals(QuestStatus.ACTIVE, result.get(0).getStatus());
    }

    @Test
    void testUpdateProgress_Success() {
        QuestProgressUpdateDto dto = new QuestProgressUpdateDto();
        dto.setQuestId(testQuest.getId());
        dto.setProgressPercentage(75);

        when(questRepository.findById(testQuest.getId())).thenReturn(Optional.of(testQuest));
        when(questProgressRepository.findByQuestAndUser(testQuest, testUser)).thenReturn(Optional.of(testProgress));
        when(questProgressRepository.save(any(QuestProgress.class))).thenReturn(testProgress);

        QuestProgress result = questService.updateProgress(dto, testUser);

        assertNotNull(result);
        verify(questProgressRepository, times(1)).save(any(QuestProgress.class));
    }

    @Test
    void testUpdateProgress_QuestNotFound() {
        QuestProgressUpdateDto dto = new QuestProgressUpdateDto();
        dto.setQuestId(UUID.randomUUID());
        dto.setProgressPercentage(75);

        when(questRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            questService.updateProgress(dto, testUser);
        });
    }

    @Test
    void testUpdateProgress_UserDoesNotOwnQuest() {
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());

        QuestProgressUpdateDto dto = new QuestProgressUpdateDto();
        dto.setQuestId(testQuest.getId());
        dto.setProgressPercentage(75);

        when(questRepository.findById(testQuest.getId())).thenReturn(Optional.of(testQuest));

        assertThrows(IllegalArgumentException.class, () -> {
            questService.updateProgress(dto, otherUser);
        });
    }

    @Test
    void testUpdateProgress_QuestNotActive() {
        testQuest.setStatus(QuestStatus.COMPLETED);

        QuestProgressUpdateDto dto = new QuestProgressUpdateDto();
        dto.setQuestId(testQuest.getId());
        dto.setProgressPercentage(75);

        when(questRepository.findById(testQuest.getId())).thenReturn(Optional.of(testQuest));

        assertThrows(IllegalArgumentException.class, () -> {
            questService.updateProgress(dto, testUser);
        });
    }

    @Test
    void testUpdateProgress_CompletesQuest() {
        QuestProgressUpdateDto dto = new QuestProgressUpdateDto();
        dto.setQuestId(testQuest.getId());
        dto.setProgressPercentage(100);

        when(questRepository.findById(testQuest.getId())).thenReturn(Optional.of(testQuest));
        when(questProgressRepository.findByQuestAndUser(testQuest, testUser)).thenReturn(Optional.of(testProgress));
        when(questProgressRepository.save(any(QuestProgress.class))).thenReturn(testProgress);
        when(questRepository.save(any(Quest.class))).thenReturn(testQuest);
        when(userService.addExperience(testUser, 100L)).thenReturn(testUser);
        when(userService.updateUser(testUser)).thenReturn(testUser);
        doNothing().when(achievementService).checkAndAwardAchievements(testUser);
        doNothing().when(questAnalyticsClient).recordQuestCompletion(any(), any(), any());

        questService.updateProgress(dto, testUser);

        verify(questRepository, times(1)).save(any(Quest.class));
    }

    @Test
    void testCompleteQuest() {
        when(questRepository.save(any(Quest.class))).thenReturn(testQuest);
        when(userService.addExperience(testUser, 100L)).thenReturn(testUser);
        when(userService.updateUser(testUser)).thenReturn(testUser);
        doNothing().when(achievementService).checkAndAwardAchievements(testUser);
        doNothing().when(questAnalyticsClient).recordQuestCompletion(any(), any(), any());

        questService.completeQuest(testQuest, testUser);

        assertEquals(QuestStatus.COMPLETED, testQuest.getStatus());
        verify(questAnalyticsClient, times(1)).recordQuestCompletion(any(), any(), any());
    }

    @Test
    void testCompleteQuest_NotActive() {
        testQuest.setStatus(QuestStatus.COMPLETED);

        assertThrows(IllegalArgumentException.class, () -> {
            questService.completeQuest(testQuest, testUser);
        });
    }

    @Test
    void testDeleteQuest_Success() {
        when(questRepository.findById(testQuest.getId())).thenReturn(Optional.of(testQuest));
        doNothing().when(questRepository).delete(testQuest);

        questService.deleteQuest(testQuest.getId(), testUser);

        verify(questRepository, times(1)).delete(testQuest);
    }

    @Test
    void testDeleteQuest_NotFound() {
        when(questRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            questService.deleteQuest(UUID.randomUUID(), testUser);
        });
    }

    @Test
    void testDeleteQuest_UserDoesNotOwn() {
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());

        when(questRepository.findById(testQuest.getId())).thenReturn(Optional.of(testQuest));

        assertThrows(IllegalArgumentException.class, () -> {
            questService.deleteQuest(testQuest.getId(), otherUser);
        });
    }

    @Test
    void testFindExpiredQuests() {
        List<Quest> expiredQuests = Arrays.asList(testQuest);
        when(questRepository.findByStatusAndEndDateBefore(QuestStatus.ACTIVE, LocalDate.now())).thenReturn(expiredQuests);

        List<Quest> result = questService.findExpiredQuests();

        assertEquals(1, result.size());
    }

    @Test
    void testExpireQuests() {
        List<Quest> expiredQuests = Arrays.asList(testQuest);
        when(questRepository.saveAll(expiredQuests)).thenReturn(expiredQuests);

        questService.expireQuests(expiredQuests);

        assertEquals(QuestStatus.EXPIRED, testQuest.getStatus());
        verify(questRepository, times(1)).saveAll(expiredQuests);
    }
}