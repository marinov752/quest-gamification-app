package com.questgamification.controller;

import com.questgamification.domain.entity.Notification;
import com.questgamification.domain.entity.User;
import com.questgamification.service.NotificationService;
import com.questgamification.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping
    public String notifications(Model model, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("Unauthenticated user tried to access notifications");
                return "redirect:/login";
            }

            String username = authentication.getName();
            if (username == null || username.isEmpty() || "anonymousUser".equals(username)) {
                logger.warn("Invalid authentication: {}", username);
                return "redirect:/login";
            }

            User user = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
            
            List<Notification> allNotifications = notificationService.getUserNotifications(user);
            
            long unreadCountBefore = notificationService.getUnreadCount(user);
            if (unreadCountBefore > 0) {
                notificationService.markAllAsRead(user);
                logger.info("Auto-marked {} notifications as read for user {}", unreadCountBefore, user.getUsername());
            }
            
            model.addAttribute("notifications", allNotifications != null ? allNotifications : Collections.emptyList());
            model.addAttribute("unreadCount", 0L);
            
            logger.info("User {} viewing notifications page. Found {} notifications", 
                user.getUsername(), 
                allNotifications != null ? allNotifications.size() : 0);
            return "notifications";
        } catch (IllegalArgumentException e) {
            logger.error("Illegal argument in notifications page: {}", e.getMessage(), e);
            model.addAttribute("notifications", Collections.emptyList());
            model.addAttribute("unreadCount", 0L);
            model.addAttribute("error", "User not found. Please log in again.");
            return "notifications";
        } catch (Exception e) {
            logger.error("Unexpected error loading notifications page: {}", e.getMessage(), e);
            model.addAttribute("notifications", Collections.emptyList());
            model.addAttribute("unreadCount", 0L);
            model.addAttribute("error", "Failed to load notifications. Please try again later.");
            return "notifications";
        }
    }

    @PostMapping("/{id}/read")
    @ResponseBody
    public String markAsRead(@PathVariable UUID id, Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            notificationService.markAsRead(id, user);
            logger.info("User {} marked notification {} as read", user.getUsername(), id);
            return "OK";
        } catch (Exception e) {
            logger.error("Error marking notification as read: {}", e.getMessage(), e);
            return "ERROR";
        }
    }

    @PostMapping("/read-all")
    public String markAllAsRead(Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            notificationService.markAllAsRead(user);
            logger.info("User {} marked all notifications as read", user.getUsername());
            redirectAttributes.addFlashAttribute("success", "All notifications marked as read!");
        } catch (Exception e) {
            logger.error("Error marking all notifications as read: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to mark all notifications as read.");
        }
        return "redirect:/notifications";
    }

    @PostMapping("/{id}/delete")
    public String deleteNotification(@PathVariable UUID id, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            notificationService.deleteNotification(id, user);
            logger.info("User {} deleted notification {}", user.getUsername(), id);
            redirectAttributes.addFlashAttribute("success", "Notification deleted!");
        } catch (Exception e) {
            logger.error("Error deleting notification: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to delete notification.");
        }
        return "redirect:/notifications";
    }

    @GetMapping("/count")
    @ResponseBody
    public long getUnreadCount(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return 0L;
            }
            User user = userService.findByUsername(authentication.getName())
                .orElse(null);
            if (user == null) {
                return 0L;
            }
            return notificationService.getUnreadCount(user);
        } catch (Exception e) {
            logger.error("Error getting unread count: {}", e.getMessage(), e);
            return 0L;
        }
    }
}
