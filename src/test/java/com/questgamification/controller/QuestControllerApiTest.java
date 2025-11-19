package com.questgamification.controller;

import com.questgamification.domain.entity.Quest;
import com.questgamification.domain.entity.QuestStatus;
import com.questgamification.domain.entity.QuestType;
import com.questgamification.domain.entity.User;
import com.questgamification.service.QuestService;
import com.questgamification.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QuestController.class)
class QuestControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuestService questService;

    @MockBean
    private UserService userService;

    @Test
    @WithMockUser
    void testGetCreateQuestForm() throws Exception {
        mockMvc.perform(get("/quests/create"))
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

        when(questService.findById(questId)).thenReturn(Optional.of(quest));

        mockMvc.perform(get("/quests/" + questId))
            .andExpect(status().isOk())
            .andExpect(view().name("quest-details"))
            .andExpect(model().attributeExists("quest"));
    }
}

