package com.questgamification.config;

import com.questgamification.domain.entity.User;
import com.questgamification.service.NotificationService;
import com.questgamification.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttribute {

    private final NotificationService notificationService;
    private final UserService userService;

    public GlobalModelAttribute(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @ModelAttribute("unreadNotificationsCount")
    public long addNotificationCount() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getName())) {
                return 0L;
            }
            
            String username = authentication.getName();
            if (username == null || username.isEmpty()) {
                return 0L;
            }
            
            User user = userService.findByUsername(username).orElse(null);
            if (user == null) {
                return 0L;
            }
            
            return notificationService.getUnreadCount(user);
        } catch (Exception e) {
            return 0L;
        }
    }
}

