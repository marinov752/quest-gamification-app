package com.questgamification.controller;

import com.questgamification.domain.entity.User;
import com.questgamification.service.StatsService;
import com.questgamification.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/stats")
public class StatsController {

    private final StatsService statsService;
    private final UserService userService;

    public StatsController(StatsService statsService, UserService userService) {
        this.statsService = statsService;
        this.userService = userService;
    }

    @GetMapping
    public String stats(Model model, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Map<String, Object> stats = statsService.getUserStats(user);
        Map<String, Object> analytics = statsService.getAnalyticsData(user.getId());
        
        model.addAttribute("user", user);
        model.addAttribute("stats", stats);
        model.addAttribute("analytics", analytics);
        
        return "stats";
    }
}

