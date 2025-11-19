package com.questgamification.domain.dto;

import com.questgamification.domain.entity.QuestType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class QuestCreateDto {

    @NotBlank
    @Size(min = 3, max = 200)
    private String title;

    @Size(max = 1000)
    private String description;

    @NotNull
    private QuestType questType;

    @NotNull
    @Positive
    private Long experienceReward;

    @NotNull
    private LocalDate startDate;

    @NotNull
    @Future
    private LocalDate endDate;

    public QuestCreateDto() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public QuestType getQuestType() {
        return questType;
    }

    public void setQuestType(QuestType questType) {
        this.questType = questType;
    }

    public Long getExperienceReward() {
        return experienceReward;
    }

    public void setExperienceReward(Long experienceReward) {
        this.experienceReward = experienceReward;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}

