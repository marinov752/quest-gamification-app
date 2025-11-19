package com.questgamification.controller;

import com.questgamification.domain.dto.QuestCreateDto;
import com.questgamification.domain.dto.QuestProgressUpdateDto;
import com.questgamification.domain.entity.Quest;
import com.questgamification.domain.entity.QuestStatus;
import com.questgamification.domain.entity.User;
import com.questgamification.service.QuestService;
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

    public QuestController(QuestService questService, UserService userService) {
        this.questService = questService;
        this.userService = userService;
    }

    @GetMapping("/create")
    public String createQuestForm(Model model) {
        model.addAttribute("questCreateDto", new QuestCreateDto());
        return "quest-create";
    }

    @PostMapping("/create")
    public String createQuest(@Valid @ModelAttribute QuestCreateDto questCreateDto,
                             BindingResult bindingResult,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
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
        Quest quest = questService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Quest not found"));
        model.addAttribute("quest", quest);
        return "quest-details";
    }

    @GetMapping("/{id}/update-progress")
    public String updateProgressForm(@PathVariable UUID id, Model model) {
        Quest quest = questService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Quest not found"));
        QuestProgressUpdateDto progressDto = new QuestProgressUpdateDto();
        progressDto.setQuestId(id);
        model.addAttribute("quest", quest);
        model.addAttribute("progressDto", progressDto);
        return "quest-update-progress";
    }

    @PostMapping("/update-progress")
    public String updateProgress(@Valid @ModelAttribute QuestProgressUpdateDto progressDto,
                                BindingResult bindingResult,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "redirect:/quests/" + progressDto.getQuestId() + "/update-progress";
        }

        try {
            User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            questService.updateProgress(progressDto, user);
            redirectAttributes.addFlashAttribute("success", "Progress updated successfully!");
            return "redirect:/quests/" + progressDto.getQuestId();
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/quests/" + progressDto.getQuestId() + "/update-progress";
        }
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

