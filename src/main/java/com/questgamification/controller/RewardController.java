package com.questgamification.controller;

import com.questgamification.domain.entity.User;
import com.questgamification.service.RewardService;
import com.questgamification.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/rewards")
public class RewardController {

    private static final Logger logger = LoggerFactory.getLogger(RewardController.class);
    private final RewardService rewardService;
    private final UserService userService;

    public RewardController(RewardService rewardService, UserService userService) {
        this.rewardService = rewardService;
        this.userService = userService;
    }

    @GetMapping
    public String rewards(Model model, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        model.addAttribute("availableRewards", rewardService.getAvailableRewards(user.getLevel()));
        model.addAttribute("claimedRewards", user.getClaimedRewards());
        return "rewards";
    }

    @PostMapping("/{id}/claim")
    public String claimReward(@PathVariable UUID id,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            rewardService.claimReward(id, user);
            userService.updateUser(user);
            redirectAttributes.addFlashAttribute("success", "Reward claimed successfully!");
            return "redirect:/rewards";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/rewards";
        }
    }
}

