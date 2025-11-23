package com.questgamification.controller;

import com.questgamification.domain.entity.QuestStatus;
import com.questgamification.domain.entity.User;
import com.questgamification.service.QuestService;
import com.questgamification.service.StatsService;
import com.questgamification.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    private final UserService userService;
    private final QuestService questService;
    private final StatsService statsService;

    public DashboardController(UserService userService, QuestService questService, StatsService statsService) {
        this.userService = userService;
        this.questService = questService;
        this.statsService = statsService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        List<com.questgamification.domain.entity.Quest> activeQuests = questService.findByUserAndStatus(user, QuestStatus.ACTIVE);
        List<com.questgamification.domain.entity.Quest> readyForCheckIn = questService.getQuestsReadyForCheckIn(user);
        Map<String, Object> stats = statsService.getUserStats(user);
        
        model.addAttribute("user", user);
        model.addAttribute("activeQuests", activeQuests);
        model.addAttribute("readyForCheckIn", readyForCheckIn);
        model.addAttribute("stats", stats);
        model.addAttribute("pageTitle", "Dashboard");
        
        return "dashboard";
    }
}

