package com.questgamification.controller;

import com.questgamification.domain.entity.Role;
import com.questgamification.domain.entity.User;
import com.questgamification.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public String manageUsers(Model model) {
        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        return "admin-users";
    }

    @PostMapping("/users/{id}/role")
    public String updateUserRole(@PathVariable UUID id,
                                @RequestParam Role role,
                                RedirectAttributes redirectAttributes) {
        try {
            userService.updateUserRole(id, role);
            redirectAttributes.addFlashAttribute("success", "User role updated successfully!");
            return "redirect:/admin/users";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/users";
        }
    }
}

