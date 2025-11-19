package com.questgamification.service;

import com.questgamification.domain.entity.Reward;
import com.questgamification.domain.entity.User;
import com.questgamification.repository.RewardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RewardService {

    private static final Logger logger = LoggerFactory.getLogger(RewardService.class);
    private final RewardRepository rewardRepository;

    public RewardService(RewardRepository rewardRepository) {
        this.rewardRepository = rewardRepository;
    }

    @Cacheable(value = "rewards", key = "#level")
    public List<Reward> getAvailableRewards(Integer level) {
        return rewardRepository.findByRequiredLevelLessThanEqualOrderByRequiredLevelAsc(level);
    }

    @Transactional
    public Reward claimReward(UUID rewardId, User user) {
        logger.info("User {} attempting to claim reward {}", user.getUsername(), rewardId);
        
        Reward reward = rewardRepository.findById(rewardId)
            .orElseThrow(() -> new IllegalArgumentException("Reward not found"));

        if (user.getLevel() < reward.getRequiredLevel()) {
            throw new IllegalArgumentException("User level is too low to claim this reward");
        }

        if (user.getExperiencePoints() < reward.getRequiredExperience()) {
            throw new IllegalArgumentException("User does not have enough experience to claim this reward");
        }

        if (user.getClaimedRewards().contains(reward)) {
            throw new IllegalArgumentException("Reward already claimed");
        }

        user.getClaimedRewards().add(reward);
        reward.getUsers().add(user);
        reward.setIsClaimed(true);

        rewardRepository.save(reward);
        logger.info("Reward {} claimed successfully by user {}", rewardId, user.getUsername());
        return reward;
    }
}

