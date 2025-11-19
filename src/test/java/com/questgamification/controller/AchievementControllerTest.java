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

@WebMvcTest(AchievementController.class)
class AchievementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AchievementService achievementService;

    @MockBean
    private UserService userService;

    @Test
    @WithMockUser
    void testAchievementsPage() throws Exception {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setAchievements(new HashSet<>());

        when(userService.findByUsername("user")).thenReturn(Optional.of(user));
        when(achievementService.getAllAchievements()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/achievements"))
                .andExpect(status().isOk())
                .andExpect(view().name("achievements"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("allAchievements"));
    }
}