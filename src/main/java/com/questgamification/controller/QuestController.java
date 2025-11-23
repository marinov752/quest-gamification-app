package com.questgamification.controller;

import com.questgamification.domain.dto.QuestCreateDto;
import com.questgamification.domain.dto.QuestProgressUpdateDto;
import com.questgamification.domain.entity.Quest;
import com.questgamification.domain.entity.QuestStatus;
import com.questgamification.domain.entity.User;
import com.questgamification.service.QuestService;
import com.questgamification.service.RewardService;
import com.questgamification.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/quests")
public class QuestController {

    private static final Logger logger = LoggerFactory.getLogger(QuestController.class);
    private final QuestService questService;
    private final UserService userService;
    private final RewardService rewardService;

    public QuestController(QuestService questService, UserService userService, RewardService rewardService) {
        this.questService = questService;
        this.userService = userService;
        this.rewardService = rewardService;
    }

    @GetMapping("/create")
    public String createQuestForm(Model model, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        model.addAttribute("questCreateDto", new QuestCreateDto());
        model.addAttribute("availableRewards", rewardService.getAvailableRewards(user.getLevel()));
        return "quest-create";
    }

    @PostMapping("/create")
    public String createQuest(@Valid @ModelAttribute QuestCreateDto questCreateDto,
                             BindingResult bindingResult,
                             Authentication authentication,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            try {
                User user = userService.findByUsername(authentication.getName())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
                model.addAttribute("availableRewards", rewardService.getAvailableRewards(user.getLevel()));
            } catch (Exception e) {
            }
            return "quest-create";
        }

        try {
            User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            questService.createQuest(questCreateDto, user);
            redirectAttributes.addFlashAttribute("success", "Quest created successfully!");
            return "redirect:/quests/my-quests";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/quests/create";
        }
    }

    @GetMapping("/my-quests")
    public String myQuests(Model model, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        List<Quest> quests = questService.findByUser(user);
        model.addAttribute("quests", quests);
        return "my-quests";
    }

    @GetMapping("/{id}")
    public String questDetails(@PathVariable UUID id, Model model, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Quest quest = questService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Quest not found"));
        
        List<com.questgamification.domain.entity.CheckIn> checkIns = questService.getCheckInsForQuest(quest, user);
        java.time.LocalDate today = java.time.LocalDate.now();
        boolean canCheckInToday = questService.canCheckIn(quest, user, today);
        
        model.addAttribute("quest", quest);
        model.addAttribute("checkIns", checkIns);
        model.addAttribute("canCheckInToday", canCheckInToday);
        model.addAttribute("checkInCount", checkIns.size());
        model.addAttribute("questStartDate", quest.getStartDate());
        model.addAttribute("questEndDate", quest.getEndDate());
        
        return "quest-details";
    }

    @GetMapping("/{id}/update-progress")
    public String updateProgressForm(@PathVariable UUID id, Model model, Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            Quest quest = questService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Quest not found"));
            
            if (!quest.getUser().getId().equals(user.getId())) {
                throw new IllegalArgumentException("You do not have permission to update this quest");
            }
            
            if (quest.getStatus() != QuestStatus.ACTIVE) {
                throw new IllegalArgumentException("Only active quests can have their progress updated");
            }
            
            QuestProgressUpdateDto progressDto = new QuestProgressUpdateDto();
            progressDto.setQuestId(id);
            model.addAttribute("quest", quest);
            model.addAttribute("progressDto", progressDto);
            return "quest-update-progress";
        } catch (IllegalArgumentException e) {
            logger.error("Error loading update progress form: {}", e.getMessage());
            throw e;
        }
    }

    @PostMapping("/{id}/check-in")
    public String checkIn(@PathVariable UUID id,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            questService.checkIn(id, user);
            redirectAttributes.addFlashAttribute("success", "Check-in recorded successfully!");
            return "redirect:/quests/" + id;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/quests/" + id;
        }
    }
    
    @PostMapping("/{id}/quick-check-in")
    public String quickCheckIn(@PathVariable UUID id,
                               @RequestParam(value = "returnTo", defaultValue = "/dashboard") String returnTo,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            questService.checkIn(id, user);
            redirectAttributes.addFlashAttribute("success", "Checked in successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:" + returnTo;
    }

    @PostMapping("/update-progress")
    public String updateProgress(@Valid @ModelAttribute QuestProgressUpdateDto progressDto,
                                BindingResult bindingResult,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "This feature has been replaced with check-ins. Please use the check-in button on the quest details page.");
        return "redirect:/quests/" + progressDto.getQuestId();
    }

    @PostMapping("/{id}/delete")
    public String deleteQuest(@PathVariable UUID id,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            questService.deleteQuest(id, user);
            redirectAttributes.addFlashAttribute("success", "Quest deleted successfully!");
            return "redirect:/quests/my-quests";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/quests/my-quests";
        }
    }
}

