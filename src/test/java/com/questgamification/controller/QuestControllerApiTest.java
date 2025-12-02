package com.questgamification.controller;

import com.questgamification.domain.entity.Quest;
import com.questgamification.domain.entity.QuestStatus;
import com.questgamification.domain.entity.QuestType;
import com.questgamification.domain.entity.User;
import com.questgamification.service.QuestService;
import com.questgamification.service.RewardService;
import com.questgamification.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import com.questgamification.config.TestSecurityConfig;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(controllers = QuestController.class)
@Import(TestSecurityConfig.class)
class QuestControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuestService questService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private RewardService rewardService;

    @Test
    @WithMockUser
    void testGetCreateQuestForm() throws Exception {
        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setUsername("user");
        mockUser.setLevel(1);
        when(userService.findByUsername("user")).thenReturn(java.util.Optional.of(mockUser));
        when(rewardService.getAvailableRewards(1)).thenReturn(new java.util.ArrayList<>());
        
        mockMvc.perform(get("/quests/create").with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("quest-create"));
    }

    @Test
    @WithMockUser
    void testGetQuestDetails() throws Exception {
        UUID questId = UUID.randomUUID();
        Quest quest = new Quest();
        quest.setId(questId);
        quest.setTitle("Test Quest");
        quest.setStatus(QuestStatus.ACTIVE);
        quest.setQuestType(QuestType.DAILY);
        quest.setExperienceReward(100L);
        quest.setStartDate(LocalDate.now());
        quest.setEndDate(LocalDate.now().plusDays(1));

        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setUsername("user");
        when(userService.findByUsername("user")).thenReturn(java.util.Optional.of(mockUser));
        when(questService.findById(questId)).thenReturn(Optional.of(quest));
        when(questService.getCheckInsForQuest(quest, mockUser)).thenReturn(new java.util.ArrayList<>());
        when(questService.canCheckIn(quest, mockUser, LocalDate.now())).thenReturn(true);

        mockMvc.perform(get("/quests/" + questId).with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("quest-details"))
            .andExpect(model().attributeExists("quest"));
    }
}

