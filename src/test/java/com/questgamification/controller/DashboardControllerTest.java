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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private QuestService questService;

    @MockBean
    private StatsService statsService;

    @Test
    @WithMockUser
    void testDashboard() throws Exception {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setLevel(1);

        when(userService.findByUsername("user")).thenReturn(Optional.of(user));
        when(questService.findByUserAndStatus(any(User.class), any(QuestStatus.class))).thenReturn(new ArrayList<>());
        when(statsService.getUserStats(any(User.class))).thenReturn(new HashMap<>());

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("activeQuests"))
                .andExpect(model().attributeExists("stats"));
    }
}