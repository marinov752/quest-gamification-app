package com.questgamification.repository;

import com.questgamification.domain.entity.Reward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RewardRepository extends JpaRepository<Reward, UUID> {
    List<Reward> findByRequiredLevelLessThanEqualOrderByRequiredLevelAsc(Integer level);
}

