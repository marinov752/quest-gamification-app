package com.questgamification.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "quest-analytics-service", url = "http://localhost:8081")
public interface QuestAnalyticsClient {

    @PostMapping("/api/analytics/quest-completion")
    void recordQuestCompletion(@RequestParam UUID userId, @RequestParam UUID questId, @RequestParam Long experiencePoints);

    @PutMapping("/api/analytics/user-stats")
    void updateUserStatistics(@RequestBody UserStatsUpdateDto statsDto);

    @org.springframework.web.bind.annotation.DeleteMapping("/api/analytics/user/{userId}")
    void deleteAnalyticsData(@org.springframework.web.bind.annotation.PathVariable java.util.UUID userId);

    @org.springframework.web.bind.annotation.GetMapping("/api/analytics/user/{userId}")
    java.util.Map<String, Object> getAnalyticsData(@org.springframework.web.bind.annotation.PathVariable java.util.UUID userId);
}

