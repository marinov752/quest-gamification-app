package com.questgamification.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "rewards")
public class Reward {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Size(min = 3, max = 200)
    @Column(nullable = false)
    private String name;

    @Size(max = 500)
    @Column(length = 500)
    private String description;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer requiredLevel;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Long requiredExperience;

    @NotNull
    @Column(nullable = false)
    private Boolean isClaimed = false;

    @ManyToMany(mappedBy = "claimedRewards")
    private Set<User> users = new HashSet<>();

    public Reward() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public Boolean getIsClaimed() {
        return isClaimed;
    }

    public void setIsClaimed(Boolean isClaimed) {
        this.isClaimed = isClaimed;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }
}

