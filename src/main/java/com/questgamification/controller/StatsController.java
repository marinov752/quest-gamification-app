package com.questgamification.controller;

import com.questgamification.domain.entity.User;
import com.questgamification.service.StatsService;
import com.questgamification.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/stats")
public class StatsController {

    private static final Logger logger = LoggerFactory.getLogger(StatsController.class);
    private final StatsService statsService;
    private final UserService userService;

    public StatsController(StatsService statsService, UserService userService) {
        this.statsService = statsService;
        this.userService = userService;
    }

    @GetMapping
    public String stats(Model model, Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            Map<String, Object> stats = statsService.getUserStats(user);
            Map<String, Object> analytics = statsService.getAnalyticsData(user.getId());
            
            model.addAttribute("user", user);
            model.addAttribute("stats", stats);
            model.addAttribute("analytics", analytics);
            
            return "stats";
        } catch (Exception e) {
            logger.error("Error loading stats page", e);
            throw e;
        }
    }
}

