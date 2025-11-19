package com.questgamification.controller;

import com.questgamification.domain.entity.*;
import com.questgamification.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RewardController.class)
class RewardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RewardService rewardService;

    @MockBean
    private UserService userService;

    @Test
    @WithMockUser
    void testRewardsPage() throws Exception {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setLevel(1);
        user.setClaimedRewards(new HashSet<>());

        when(userService.findByUsername("user")).thenReturn(Optional.of(user));
        when(rewardService.getAvailableRewards(1)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/rewards"))
                .andExpect(status().isOk())
                .andExpect(view().name("rewards"))
                .andExpect(model().attributeExists("availableRewards"))
                .andExpect(model().attributeExists("claimedRewards"));
    }

    @Test
    @WithMockUser
    void testClaimReward() throws Exception {
        UUID rewardId = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());
        Reward reward = new Reward();

        when(userService.findByUsername("user")).thenReturn(Optional.of(user));
        when(rewardService.claimReward(rewardId, user)).thenReturn(reward);
        when(userService.updateUser(user)).thenReturn(user);

        mockMvc.perform(post("/rewards/" + rewardId + "/claim")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rewards"));

        verify(rewardService, times(1)).claimReward(rewardId, user);
    }
}