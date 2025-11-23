package com.questgamification.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class RewardCreateDto {

    @NotBlank
    @Size(min = 3, max = 200)
    private String name;

    @Size(max = 500)
    private String description;

    @NotNull
    @Positive
    private Integer requiredLevel;

    @NotNull
    @Positive
    private Long requiredExperience;

    public RewardCreateDto() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getRequiredLevel() {
        return requiredLevel;
    }

    public void setRequiredLevel(Integer requiredLevel) {
        this.requiredLevel = requiredLevel;
    }

    public Long getRequiredExperience() {
        return requiredExperience;
    }

    public void setRequiredExperience(Long requiredExperience) {
        this.requiredExperience = requiredExperience;
    }
}

