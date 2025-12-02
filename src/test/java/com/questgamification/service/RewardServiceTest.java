package com.questgamification.service;

import com.questgamification.domain.entity.Reward;
import com.questgamification.domain.entity.User;
import com.questgamification.repository.RewardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RewardServiceTest {

    @Mock
    private RewardRepository rewardRepository;

    @InjectMocks
    private RewardService rewardService;

    private User testUser;
    private Reward testReward;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setLevel(5);
        testUser.setExperiencePoints(1000L);
        testUser.setClaimedRewards(new HashSet<>());

        testReward = new Reward();
        testReward.setId(UUID.randomUUID());
        testReward.setName("Test Reward");
        testReward.setDescription("Test Description");
        testReward.setRequiredLevel(3);
        testReward.setRequiredExperience(500L);
        testReward.setIsClaimed(false);
        testReward.setUsers(new HashSet<>());
    }

    @Test
    void testGetAvailableRewards() {
        List<Reward> rewards = Arrays.asList(testReward);
        when(rewardRepository.findByRequiredLevelLessThanEqualOrderByRequiredLevelAsc(5)).thenReturn(rewards);

        List<Reward> result = rewardService.getAvailableRewards(5);

        assertEquals(1, result.size());
        assertEquals(testReward.getName(), result.get(0).getName());
    }

    @Test
    void testClaimReward_Success() {
        when(rewardRepository.findById(testReward.getId())).thenReturn(Optional.of(testReward));
        when(rewardRepository.save(any(Reward.class))).thenReturn(testReward);

        Reward result = rewardService.claimReward(testReward.getId(), testUser);

        assertNotNull(result);
        // The implementation adds user to reward's users list, not setting IsClaimed flag
        assertTrue(result.getUsers().contains(testUser));
        verify(rewardRepository, times(1)).save(testReward);
    }

    @Test
    void testClaimReward_NotFound() {
        when(rewardRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            rewardService.claimReward(UUID.randomUUID(), testUser);
        });
    }

    @Test
    void testClaimReward_LevelTooLow() {
        testUser.setLevel(1);
        when(rewardRepository.findById(testReward.getId())).thenReturn(Optional.of(testReward));

        assertThrows(IllegalArgumentException.class, () -> {
            rewardService.claimReward(testReward.getId(), testUser);
        });
    }

    @Test
    void testClaimReward_NotEnoughExperience() {
        testUser.setExperiencePoints(100L);
        when(rewardRepository.findById(testReward.getId())).thenReturn(Optional.of(testReward));

        assertThrows(IllegalArgumentException.class, () -> {
            rewardService.claimReward(testReward.getId(), testUser);
        });
    }

    @Test
    void testClaimReward_AlreadyClaimed() {
        testUser.getClaimedRewards().add(testReward);
        when(rewardRepository.findById(testReward.getId())).thenReturn(Optional.of(testReward));

        assertThrows(IllegalArgumentException.class, () -> {
            rewardService.claimReward(testReward.getId(), testUser);
        });
    }
}