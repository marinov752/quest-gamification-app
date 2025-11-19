package com.questgamification.controller;

import com.questgamification.domain.entity.User;
import com.questgamification.service.AchievementService;
import com.questgamification.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/achievements")
public class AchievementController {

    private final AchievementService achievementService;
    private final UserService userService;

    public AchievementController(AchievementService achievementService, UserService userService) {
        this.achievementService = achievementService;
        this.userService = userService;
    }

    @GetMapping
    public String achievements(Model model, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        model.addAttribute("user", user);
        model.addAttribute("allAchievements", achievementService.getAllAchievements());
        
        return "achievements";
    }
}

