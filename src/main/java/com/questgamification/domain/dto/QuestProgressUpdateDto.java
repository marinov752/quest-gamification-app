package com.questgamification.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class QuestProgressUpdateDto {

    @NotNull
    private UUID questId;

    @NotNull
    @Min(0)
    @Max(100)
    private Integer progressPercentage;

    public QuestProgressUpdateDto() {
    }

    public UUID getQuestId() {
        return questId;
    }

    public void setQuestId(UUID questId) {
        this.questId = questId;
    }

    public Integer getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(Integer progressPercentage) {
        this.progressPercentage = progressPercentage;
    }
}

