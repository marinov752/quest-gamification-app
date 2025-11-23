package com.questgamification.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "quests")
public class Quest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Size(min = 3, max = 200)
    @Column(nullable = false)
    private String title;

    @Size(max = 1000)
    @Column(length = 1000)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestType questType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestStatus status;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Long experienceReward;

    @NotNull
    @Column(nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(nullable = false)
    private LocalDate endDate;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer checkInGoal; // Number of check-ins required to complete the quest

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "quest", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<QuestProgress> progressRecords = new HashSet<>();

    @OneToMany(mappedBy = "quest", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<com.questgamification.domain.entity.CheckIn> checkIns = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "quest_rewards",
        joinColumns = @JoinColumn(name = "quest_id"),
        inverseJoinColumns = @JoinColumn(name = "reward_id")
    )
    private Set<Reward> rewards = new HashSet<>();

    public Quest() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public QuestStatus getStatus() {
        return status;
    }

    public void setStatus(QuestStatus status) {
        this.status = status;
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

    public Integer getCheckInGoal() {
        return checkInGoal;
    }

    public void setCheckInGoal(Integer checkInGoal) {
        this.checkInGoal = checkInGoal;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<QuestProgress> getProgressRecords() {
        return progressRecords;
    }

    public void setProgressRecords(Set<QuestProgress> progressRecords) {
        this.progressRecords = progressRecords;
    }

    public Set<Reward> getRewards() {
        return rewards;
    }

    public void setRewards(Set<Reward> rewards) {
        this.rewards = rewards;
    }

    public Set<com.questgamification.domain.entity.CheckIn> getCheckIns() {
        return checkIns;
    }

    public void setCheckIns(Set<com.questgamification.domain.entity.CheckIn> checkIns) {
        this.checkIns = checkIns;
    }
}

