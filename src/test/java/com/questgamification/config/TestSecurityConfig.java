package com.questgamification.config;

import com.questgamification.repository.AchievementRepository;
import com.questgamification.repository.QuestRepository;
import com.questgamification.repository.RewardRepository;
import com.questgamification.repository.UserRepository;
import com.questgamification.service.NotificationService;
import com.questgamification.service.QuestService;
import com.questgamification.service.RewardService;
import com.questgamification.service.UserService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    @Bean
    public NotificationService notificationService() {
        return Mockito.mock(NotificationService.class);
    }

    @Bean
    public UserService userService() {
        return Mockito.mock(UserService.class);
    }

    @Bean
    public QuestService questService() {
        return Mockito.mock(QuestService.class);
    }

    @Bean
    public RewardService rewardService() {
        return Mockito.mock(RewardService.class);
    }

    @Bean
    public QuestRepository questRepository() {
        return Mockito.mock(QuestRepository.class);
    }

    @Bean
    public RewardRepository rewardRepository() {
        return Mockito.mock(RewardRepository.class);
    }

    @Bean
    public AchievementRepository achievementRepository() {
        return Mockito.mock(AchievementRepository.class);
    }

    @Bean
    public UserRepository userRepository() {
        return Mockito.mock(UserRepository.class);
    }

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );
        return http.build();
    }
}