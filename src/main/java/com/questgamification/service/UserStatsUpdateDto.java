package com.questgamification.service;

import java.util.UUID;

public class UserStatsUpdateDto {
    private UUID userId;
    private Long totalExperience;
    private Integer level;
    private Integer questsCompleted;

    public UserStatsUpdateDto() {
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Long getTotalExperience() {
        return totalExperience;
    }

    public void setTotalExperience(Long totalExperience) {
        this.totalExperience = totalExperience;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getQuestsCompleted() {
        return questsCompleted;
    }

    public void setQuestsCompleted(Integer questsCompleted) {
        this.questsCompleted = questsCompleted;
    }
}

