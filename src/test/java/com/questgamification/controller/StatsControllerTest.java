// src/test/java/com/questgamification/controller/StatsControllerTest.java
package com.questgamification.controller;

import com.questgamification.domain.entity.User;
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

@WebMvcTest(StatsController.class)
class StatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatsService statsService;

    @MockBean
    private UserService userService;

    @Test
    @WithMockUser
    void testStatsPage() throws Exception {
        User user = new User();
        user.setId(UUID.randomUUID());

        Map<String, Object> stats = new HashMap<>();
        stats.put("level", 5);
        stats.put("experiencePoints", 1000L);
        stats.put("totalQuests", 10);
        stats.put("completedQuests", 5L);
        stats.put("activeQuests", 3L);
        stats.put("achievementsCount", 2);

        when(userService.findByUsername("user")).thenReturn(Optional.of(user));
        when(statsService.getUserStats(any(User.class))).thenReturn(stats);
        when(statsService.getAnalyticsData(any(UUID.class))).thenReturn(new HashMap<>());

        mockMvc.perform(get("/stats"))
                .andExpect(status().isOk())
                .andExpect(view().name("stats"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("stats"))
                .andExpect(model().attributeExists("analytics"));
    }
}