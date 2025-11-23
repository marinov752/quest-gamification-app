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
        
        if (user.getAchievements() == null) {
            user.setAchievements(new java.util.HashSet<>());
        }
        
        for (com.questgamification.domain.entity.Achievement achievement : user.getAchievements()) {
            achievement.getId();
        }
        
        java.util.List<com.questgamification.domain.entity.Achievement> allAchievements = achievementService.getAllAchievements();
        
        java.util.Set<java.util.UUID> achievedIds = new java.util.HashSet<>();
        if (user.getAchievements() != null && !user.getAchievements().isEmpty()) {
            for (com.questgamification.domain.entity.Achievement achievement : user.getAchievements()) {
                if (achievement != null && achievement.getId() != null) {
                    achievedIds.add(achievement.getId());
                }
            }
        }
        
        java.util.List<com.questgamification.domain.entity.Achievement> sortedAchievements = allAchievements.stream()
            .sorted((a1, a2) -> {
                boolean a1Achieved = achievedIds.contains(a1.getId());
                boolean a2Achieved = achievedIds.contains(a2.getId());
                if (a1Achieved && !a2Achieved) return -1;
                if (!a1Achieved && a2Achieved) return 1;
                return a1.getName().compareToIgnoreCase(a2.getName());
            })
            .collect(java.util.stream.Collectors.toList());
        
        long achievedCount = achievedIds.size();
        long totalCount = allAchievements.size();
        
        model.addAttribute("user", user);
        model.addAttribute("allAchievements", sortedAchievements);
        model.addAttribute("achievedCount", achievedCount);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("achievedIds", achievedIds);
        
        return "achievements";
    }
}

