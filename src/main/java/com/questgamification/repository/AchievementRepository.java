package com.questgamification.repository;

import com.questgamification.domain.entity.Achievement;
import com.questgamification.domain.entity.AchievementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, UUID> {
    List<Achievement> findByAchievementType(AchievementType achievementType);
}

