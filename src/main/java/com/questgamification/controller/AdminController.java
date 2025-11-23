package com.questgamification.controller;

import com.questgamification.domain.dto.RewardCreateDto;
import com.questgamification.domain.entity.Quest;
import com.questgamification.domain.entity.QuestStatus;
import com.questgamification.domain.entity.Reward;
import com.questgamification.domain.entity.Role;
import com.questgamification.domain.entity.User;
import com.questgamification.repository.AchievementRepository;
import com.questgamification.repository.QuestRepository;
import com.questgamification.repository.RewardRepository;
import com.questgamification.service.RewardService;
import com.questgamification.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final UserService userService;
    private final QuestRepository questRepository;
    private final RewardRepository rewardRepository;
    private final RewardService rewardService;
    private final AchievementRepository achievementRepository;

    public AdminController(UserService userService, QuestRepository questRepository,
                          RewardRepository rewardRepository, RewardService rewardService,
                          AchievementRepository achievementRepository) {
        this.userService = userService;
        this.questRepository = questRepository;
        this.rewardRepository = rewardRepository;
        this.rewardService = rewardService;
        this.achievementRepository = achievementRepository;
    }

    @GetMapping
    public String adminDashboard(Model model) {
        logger.info("Admin dashboard accessed");
        
        long totalUsers = userService.findAll().size();
        long totalQuests = questRepository.count();
        long totalRewards = rewardRepository.count();
        long totalAchievements = achievementRepository.count();
        
        List<Quest> allQuests = questRepository.findAll();
        long activeQuests = allQuests.stream().filter(q -> q.getStatus() == QuestStatus.ACTIVE).count();
        long completedQuests = allQuests.stream().filter(q -> q.getStatus() == QuestStatus.COMPLETED).count();
        long expiredQuests = allQuests.stream().filter(q -> q.getStatus() == QuestStatus.EXPIRED).count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("totalQuests", totalQuests);
        stats.put("activeQuests", activeQuests);
        stats.put("completedQuests", completedQuests);
        stats.put("expiredQuests", expiredQuests);
        stats.put("totalRewards", totalRewards);
        stats.put("totalAchievements", totalAchievements);
        
        model.addAttribute("stats", stats);
        return "admin-dashboard";
    }

    @GetMapping("/users")
    public String manageUsers(Model model) {
        logger.info("Admin accessing user management");
        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        return "admin-users";
    }

    @PostMapping("/users/{id}/role")
    public String updateUserRole(@PathVariable UUID id,
                                @RequestParam Role role,
                                RedirectAttributes redirectAttributes) {
        try {
            logger.info("Admin updating role for user {} to {}", id, role);
            userService.updateUserRole(id, role);
            redirectAttributes.addFlashAttribute("success", "User role updated successfully!");
            return "redirect:/admin/users";
        } catch (IllegalArgumentException e) {
            logger.error("Error updating user role: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/users";
        }
    }

    @GetMapping("/quests")
    public String manageQuests(Model model) {
        logger.info("Admin accessing quest management");
        List<Quest> allQuests = questRepository.findAll();
        model.addAttribute("quests", allQuests);
        return "admin-quests";
    }

    @PostMapping("/quests/{id}/delete")
    public String deleteQuest(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            logger.info("Admin deleting quest {}", id);
            Quest quest = questRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Quest not found"));
            questRepository.delete(quest);
            redirectAttributes.addFlashAttribute("success", "Quest deleted successfully!");
            return "redirect:/admin/quests";
        } catch (IllegalArgumentException e) {
            logger.error("Error deleting quest: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/quests";
        }
    }

    @GetMapping("/rewards")
    public String manageRewards(Model model) {
        logger.info("Admin accessing reward management");
        List<Reward> rewards = rewardService.getAllRewards();
        model.addAttribute("rewards", rewards);
        return "admin-rewards";
    }

    @GetMapping("/rewards/create")
    public String createRewardForm(Model model) {
        model.addAttribute("rewardCreateDto", new RewardCreateDto());
        return "admin-reward-create";
    }

    @PostMapping("/rewards/create")
    public String createReward(@Valid @ModelAttribute RewardCreateDto rewardCreateDto,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (bindingResult.hasErrors()) {
            return "admin-reward-create";
        }

        try {
            rewardService.createReward(rewardCreateDto);
            redirectAttributes.addFlashAttribute("success", "Reward created successfully!");
            return "redirect:/admin/rewards";
        } catch (IllegalArgumentException e) {
            logger.error("Error creating reward: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/rewards/create";
        }
    }

    @GetMapping("/rewards/{id}/edit")
    public String editRewardForm(@PathVariable UUID id, Model model) {
        Reward reward = rewardService.findById(id);
        
        RewardCreateDto rewardDto = new RewardCreateDto();
        rewardDto.setName(reward.getName());
        rewardDto.setDescription(reward.getDescription());
        rewardDto.setRequiredLevel(reward.getRequiredLevel());
        rewardDto.setRequiredExperience(reward.getRequiredExperience());
        
        model.addAttribute("rewardCreateDto", rewardDto);
        model.addAttribute("rewardId", id);
        return "admin-reward-edit";
    }

    @PostMapping("/rewards/{id}/edit")
    public String updateReward(@PathVariable UUID id,
                              @Valid @ModelAttribute RewardCreateDto rewardCreateDto,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Validation errors occurred");
            return "redirect:/admin/rewards/" + id + "/edit";
        }

        try {
            rewardService.updateReward(id, rewardCreateDto);
            redirectAttributes.addFlashAttribute("success", "Reward updated successfully!");
            return "redirect:/admin/rewards";
        } catch (IllegalArgumentException e) {
            logger.error("Error updating reward: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/rewards/" + id + "/edit";
        }
    }

    @PostMapping("/rewards/{id}/delete")
    public String deleteReward(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            logger.info("Admin deleting reward {}", id);
            rewardService.deleteReward(id);
            redirectAttributes.addFlashAttribute("success", "Reward deleted successfully!");
            return "redirect:/admin/rewards";
        } catch (IllegalArgumentException e) {
            logger.error("Error deleting reward: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/rewards";
        }
    }
}

